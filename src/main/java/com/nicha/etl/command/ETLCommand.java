package com.nicha.etl.command;

import com.nicha.etl.service.ETLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.CommandScan;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
@CommandScan
@Command(command = "etl")
public class ETLCommand {

    private final ETLService etlService;

    @Autowired
    public ETLCommand(ETLService etlService) {
        this.etlService = etlService;
    }

    @Command(command = "log", description = "Print out processes along with states and required process ID")
    public void checkLogs(@Option(
            defaultValue = "0",
            longNames = "amount",
            shortNames = 'n',
            description = "How many latest logs message do you want to get. 0 or smaller will print ALL logs in database"
    ) int amount) {
        etlService.printLogMessage(amount);
    }

    @Command(command = "status", description = "Print out processes along with states and required process ID")
    public void checkProcessStatus() {
        etlService.printAllProcessTrackerStatus();
    }

    @Command(command = "run-all", description = "Run a whole ETL process cycle keyword")
    public void runProcessCycle(@Option(
            defaultValue = "false",
            description = "Optional variable, bypass successful run today",
            longNames = "force",
            shortNames = 'f'
    ) boolean forceRun) {
        try {
            etlService.run(forceRun);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Command(command = "run", description = "Run a specific process in ETL, or -n all to run all process")
    public void runProcess(
            @Option(
                    required = true,
                    longNames = "name",
                    shortNames = 'n',
                    description = "Process name must be of these value: crawl-cellphones, load-cellphones, load-staging, load-warehouse or all"
            ) String processName,
            @Option(
                    defaultValue = "false",
                    description = "Optional variable, bypass successful run today and process required condition",
                    longNames = "force",
                    shortNames = 'f'
            ) boolean forceRun) {
        try {
            switch (processName) {
                case "all":
                    etlService.run(forceRun);
                    break;
                case "crawl-cellphones":
                    etlService.runCrawlCellphoneSService(forceRun);
                    break;
                case "load-cellphones":
                    etlService.runLoadToCellphoneSStagingService(forceRun);
                    break;
                case "load-staging":
                    etlService.runTransformCellphoneSAndLoadToStagingService(forceRun);
                    break;
                case "load-warehouse":
                    etlService.loadToWarehouseService(forceRun);
                    break;
                default:
                    System.out.println("Can't run process " + processName);
                    System.out.println("Process name must be one of these value: crawl-cellphones, load-cellphones, load-staging, load-warehouse or all");
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Command(command = "run-failed", description = "Run failed processes in ETL process cycle")
    public void runFailedProcesses() {
        try {
            etlService.runFailedProcessesOnly();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }


}
