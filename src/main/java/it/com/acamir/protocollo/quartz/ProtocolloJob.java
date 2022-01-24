package it.com.acamir.protocollo.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.com.acamir.protocollo.services.ProtocolloService;

@Component
@DisallowConcurrentExecution
public class ProtocolloJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(ProtocolloService.class);
	@Autowired
	private ProtocolloService dataStreamService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			dataStreamService.creaImmagineAssociaPdf();
			// dataStreamService.associaImmagineAPdf();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}