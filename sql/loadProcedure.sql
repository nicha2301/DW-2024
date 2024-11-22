/*B1: Xử lý dữ liệu bị xóa
	+ Tìm các dòng dữ liệu ở warehouse hiện tại và tìm trong cleaned_data xem thử có không=> đổi
B2: insert các dòng có productId mới vào trước
B3: Xử lý dữ liệu bị trùng
	+ Tìm đến các dòng có productId giống nhau, so sánh các cột
	+ Nếu khác thì lưu vào 1 bảng temp_update_id
	+ Tìm tới các productId có trong bảng temp_update_id và chỉnh sửa các cột giá trị isDelete = True, expried_date = NOW(), date_delete = NOW()
	+ Insert từ cleaned_data các dòng có id trong temp_update_id vào
*/
use products_db;

drop procedure if exists load_data_to_warehouse;
DELIMITER $$

CREATE PROCEDURE load_data_to_warehouse()
BEGIN
    DECLARE curr_date DATE;
    SET curr_date = CURDATE();

    -- B1: Xử lý dữ liệu bị xóa
UPDATE head_phone AS wh
    LEFT JOIN staging_head_phone_daily AS stg
ON wh.product_id = stg.product_id
    SET wh.isDelete = TRUE,
        wh.expired_date = curr_date,
        wh.date_delete = curr_date
WHERE stg.product_id IS NULL AND wh.isDelete = FALSE;

-- B2: Insert các dòng mới (product_id chưa tồn tại trong warehouse)
INSERT INTO head_phone (
    product_id, name, brand, type, price, warranty_info,
    feature, voice_control, microphone, battery_life,
    dimensions, weight, compatibility, created_at, date_insert
)
SELECT
    stg.product_id, stg.name, stg.brand, stg.type, stg.price, stg.warranty_info,
    stg.feature, stg.voice_control, stg.microphone, stg.battery_life,
    stg.dimensions, stg.weight, stg.compatibility, stg.created_at, NOW()
FROM staging_head_phone_daily AS stg
         LEFT JOIN head_phone AS wh
                   ON stg.product_id = wh.product_id
WHERE wh.product_id IS NULL;

-- B3: Xử lý dữ liệu bị trùng lặp
CREATE TEMPORARY TABLE temp_update_id (
        product_id VARCHAR(255) NOT NULL
    );

    -- Tìm các product_id bị trùng lặp và lưu vào bảng tạm
INSERT INTO temp_update_id (product_id)
SELECT stg.product_id
FROM staging_head_phone_daily AS stg
         INNER JOIN head_phone AS wh
                    ON stg.product_id = wh.product_id
WHERE (
          stg.name <> wh.name OR
          stg.brand <> wh.brand OR
          stg.type <> wh.type OR
          stg.price <> wh.price OR
          stg.warranty_info <> wh.warranty_info OR
          stg.feature <> wh.feature OR
          stg.voice_control <> wh.voice_control OR
          stg.microphone <> wh.microphone OR
          stg.battery_life <> wh.battery_life OR
          stg.dimensions <> wh.dimensions OR
          stg.weight <> wh.weight OR
          stg.compatibility <> wh.compatibility
          );

-- Tìm tới các productId có trong bảng temp_update_id và chỉnh sửa các cột giá trị isDelete = True, expried_date = NOW(), date_delete = NOW()
-- Kiểm tra dữ liệu trong temp_update_id
    IF (SELECT COUNT(*) FROM temp_update_id) > 0 THEN
        -- Chỉ thực hiện UPDATE nếu temp_update_id có dữ liệu
        UPDATE head_phone AS wh
            INNER JOIN temp_update_id AS temp
            ON wh.product_id = temp.product_id
        SET wh.isDelete = TRUE,
            wh.expired_date = curr_date,
            wh.date_delete = curr_date;
    END IF;

-- Chèn các dòng cập nhật từ staging vào warehouse
INSERT INTO head_phone (
    product_id, name, brand, type, price, warranty_info,
    feature, voice_control, microphone, battery_life,
    dimensions, weight, compatibility, created_at, date_insert
)
SELECT
    stg.product_id, stg.name, stg.brand, stg.type, stg.price, stg.warranty_info,
    stg.feature, stg.voice_control, stg.microphone, stg.battery_life,
    stg.dimensions, stg.weight, stg.compatibility, stg.created_at, NOW()
FROM staging_head_phone_daily AS stg
         INNER JOIN temp_update_id AS temp
                    ON stg.product_id = temp.product_id;

-- Xóa bảng tạm
DROP TEMPORARY TABLE IF EXISTS temp_update_id;
END$$

DELIMITER ;