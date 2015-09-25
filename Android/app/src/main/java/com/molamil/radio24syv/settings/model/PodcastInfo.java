package com.molamil.radio24syv.settings.model;

import com.molamil.radio24syv.RadioLibrary;
import com.molamil.radio24syv.api.model.Podcast;

import java.io.Serializable;

/**
 * Information about a Podcast from the API containing only the stuff needed.
 * Dependency on the API is handled through this class. And it is also lighter to pass as serializable.
 * Created by jens on 24/09/15.
 */
public class PodcastInfo implements Serializable {
    private int podcastId;
    private int programId;
    private String title;
    private String description;
    private String date;
    private String audioUrl;

    public PodcastInfo() {}

    public PodcastInfo(Podcast podcast) {
        podcastId = podcast.getVideoPodcastId();
        programId = podcast.getProgramInfo().getId();
        title = podcast.getTitle();
        description = podcast.getDescription().getText();
        date = podcast.getPublishInfo().getCreatedAt();
        audioUrl = podcast.getAudioInfo().getUrl();
    }

    public int getPodcastId() {
        return podcastId;
    }

    public void setPodcastId(int podcastId) {
        this.podcastId = podcastId;
    }

    public int getProgramId() {
        return programId;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
}
