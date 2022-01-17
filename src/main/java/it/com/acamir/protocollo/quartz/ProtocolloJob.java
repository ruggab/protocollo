package it.com.acamir.protocollo.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.com.acamir.protocollo.services.ProtocolloService;


@Component
@DisallowConcurrentExecution
public class ProtocolloJob implements Job {

    @Autowired
    private ProtocolloService dataStreamService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
        	dataStreamService.creaImmagineDaFile();
        	dataStreamService.associaImmagineTestoApdf();
        	dataStreamService.associaBarcodeApdf();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}