package org.phenoscape.scigraph;

import java.util.List;
import java.util.Map;

public class SciGraphResponse {
	
	private Map<String, String> entityList;
	private Map<String, String> qualityList;

	public SciGraphResponse (Map<String, String> eList, Map<String, String> qList){
		entityList = eList;
		qualityList = qList;
	}
	
	public Map<String, String> getEntityList(){
		return entityList;
	}
	
	public Map<String, String> getQualityList(){
		return qualityList;
	}
}
