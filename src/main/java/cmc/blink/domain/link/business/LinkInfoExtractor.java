package cmc.blink.domain.link.business;

import cmc.blink.domain.link.presentation.dto.LinkResponse;

public interface LinkInfoExtractor {
    LinkResponse.LinkInfo extractInfo(String domain, String url) throws Exception;
}
