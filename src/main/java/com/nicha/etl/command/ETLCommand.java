package com.nicha.etl.command;

import com.nicha.etl.service.ETLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.CommandScan;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
@CommandScan
@Command(command = "etl", description = "ETL process keyword")
public class ETLCommand {

    private ETLService etlService;

    @Autowired
    public ETLCommand(ETLService etlService) {
        this.etlService = etlService;
    }

    @Command(command = "status")
    public void checkProcessStatus() {
        etlService.printAllProcessTrackerStatus();
    }

    @Command(command = "run-all")
    public void runProcessCycle(@Option(
            defaultValue = "false",
            description = "Optional variable, bypass successful run today",
            longNames = "force-run",
            shortNames = 'f'
    ) boolean forceRun) {
        try {
            etlService.run(forceRun);
        }
        catch (Exception e) {}
    }
}
