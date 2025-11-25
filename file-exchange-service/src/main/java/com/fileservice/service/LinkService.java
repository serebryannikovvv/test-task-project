package com.fileservice.service;

public class LinkService {
    private final String baseUrl;

    public LinkService(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    public String generateDownloadLink(String fileId) {
        return baseUrl + "file/" + fileId;
    }
}