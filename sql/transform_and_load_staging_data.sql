DELIMITER $$

CREATE PROCEDURE transform_and_load_staging_data()
BEGIN
    -- Bước 1: Xóa dữ liệu cũ trong staging_head_phone_daily (nếu cần)
DELETE FROM staging_head_phone_daily;

-- Bước 2: Transform và chèn dữ liệu từ staging_head_phone vào staging_head_phone_daily
INSERT INTO staging_head_phone_daily (
    product_id, NAME, brand, price, type, battery_life, compatibility, dimensions,
    feature, microphone, voice_control, warranty_info, weight, created_at
)
SELECT
    product_id,
    name,
    brand,
    -- Xử lý cột price
    CASE
        WHEN price = '0' THEN 0 -- Nếu giá là 0
        WHEN price REGEXP '^[0-9,]+$' THEN CAST(REPLACE(price, ',', '') AS DECIMAL(15, 2)) -- Loại bỏ dấu phẩy và chuyển thành số
        WHEN price REGEXP '^[0-9]+\\.[0-9]+E[+-][0-9]+$' THEN CAST(price AS DECIMAL(15, 2)) -- Dạng khoa học
        WHEN price REGEXP '^[0-9]+E[+-][0-9]+$' THEN CAST(price AS DECIMAL(15, 2)) -- Dạng exponential không chứa dấu chấm
        ELSE NULL -- Trường hợp không hợp lệ
        END AS price,
    -- Xử lý cột type
    CASE
        WHEN type IN ('-', '', 'Bàn') THEN
            CASE
                WHEN name LIKE '%không dây%' OR name LIKE '%Bluetooth%' OR name LIKE '%nhét tai%'
                    OR name LIKE '%chụp tai%' OR name LIKE '%TWS%' THEN 'Tai nghe Bluetooth'
                ELSE 'Tai nghe có dây'
                END
        ELSE type
        END AS type,
    -- Xử lý cột battery_life
    CASE
        WHEN battery_life IS NULL OR battery_life = '' THEN 'Thông tin không có'
        ELSE REPLACE(REGEXP_REPLACE(battery_life, '<[^>]*>', ' '), '\n', ', ')
        END AS battery_life,
    -- Xử lý cột compatibility
    CASE
        WHEN compatibility IS NULL OR compatibility = '' THEN 'Hỗ trợ hầu hết các thiết bị'
        ELSE REPLACE(REGEXP_REPLACE(compatibility, '<[^>]*>', ''), '\n', ', ')
        END AS compatibility,
    -- Xử lý cột dimensions
    CASE
        WHEN dimensions IS NULL OR dimensions = '' THEN 'Kích thước không xác định'
        ELSE REPLACE(REGEXP_REPLACE(dimensions, '<[^>]*>', ''), '\n', ', ')
        END AS dimensions,
    -- Xử lý cột feature
    CASE
        WHEN feature IS NULL OR feature = '' THEN 'Nghe nhạc'
        ELSE REGEXP_REPLACE(feature, '<[^>]*>', '')
        END AS feature,
    -- Xử lý cột microphone
    CASE
        WHEN microphone IS NULL OR microphone = '' THEN 'Không'
        ELSE REGEXP_REPLACE(microphone, '<[^>]*>', ' ')
        END AS microphone,
    -- Xử lý cột voice_control
    CASE
        WHEN voice_control IS NULL OR voice_control = '' THEN 'Không hỗ trợ'
        ELSE REGEXP_REPLACE(voice_control, '<[^>]*>', '')
        END AS voice_control,
    -- Xử lý cột warranty_info
    CASE
        WHEN warranty_info IS NULL OR warranty_info = '' THEN 'Bảo hành 1 đổi 1 trong 12 tháng.'
        ELSE REGEXP_REPLACE(warranty_info, '<[^>]*>', '')
        END AS warranty_info,
    -- Xử lý cột weight
    CASE
        WHEN weight IS NULL OR weight = '' THEN '500g'
        ELSE REGEXP_REPLACE(weight, '<[^>]*>', '')
        END AS weight,
    -- Xử lý cột created_at
    CASE
        WHEN created_at IS NULL OR created_at = '' THEN NULL -- Nếu dữ liệu rỗng, trả về NULL
        ELSE STR_TO_DATE(SUBSTRING_INDEX(created_at, '.', 1), '%Y-%m-%dT%H:%i:%s') -- Chuyển đổi sang DATETIME
        END AS created_at
FROM staging_head_phone;

-- Kiểm tra và ghi log nếu cần
IF (SELECT COUNT(*) FROM staging_head_phone_daily) = 0 THEN
        SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Dữ liệu không được chèn vào staging_head_phone_daily. Kiểm tra lại quy trình.';
END IF;
END$$

DELIMITER ;
