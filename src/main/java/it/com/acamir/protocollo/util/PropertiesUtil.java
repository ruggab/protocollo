package it.com.acamir.protocollo.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "batch.protocollo")
@Configuration
public class PropertiesUtil {

	private static String cronExpression;
	private static String pathDaProtocollare;
	private static String pathProtocollati;
	private static String pathDaRimuovere;
	
	
	public static String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public static String getPathDaProtocollare() {
		return pathDaProtocollare;
	}

	public void setPathDaProtocollare(String pathDaProtocollare) {
		this.pathDaProtocollare = pathDaProtocollare;
	}

	public static String getPathProtocollati() {
		return pathProtocollati;
	}

	public void setPathProtocollati(String pathProtocollati) {
		this.pathProtocollati = pathProtocollati;
	}

	public static String getPathDaRimuovere() {
		return pathDaRimuovere;
	}

	public void setPathDaRimuovere(String pathDaRimuovere) {
		this.pathDaRimuovere = pathDaRimuovere;
	}
	
	
	
	

}
