/**
 * Copyright (C) 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers;

import dao.AudioDao;
import models.Audio;
import models.AudioDto;
import models.AudioTitlesDto;
import models.AudiosDto;
import ninja.*;
import ninja.appengine.AppEngineFilter;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import etc.LoggedInUser;
import ninja.params.PathParam;

@Singleton
@FilterWith(AppEngineFilter.class)
public class ApiController {
    
    @Inject
    AudioDao audioDao;

    public Result getAudioTitlesJson() {

        AudioTitlesDto audioTitlesDto = audioDao.getAllAudioTitles();

        return Results.json().render(audioTitlesDto);

    }

    ///////////////////////////////////////////////////////////////////////////
    // Show audio
    ///////////////////////////////////////////////////////////////////////////
    public Result getAudioByIdJson(@PathParam("id") Long id) {

        Audio audio = null;
        AudioDto audioDto = null;

        if (id != null) {

            audio = audioDao.getAudio(id);
            audioDto = new AudioDto();
            audioDto.duration = audio.duration;
            audioDto.fullPath = audio.fullPath;
            audioDto.title = audio.title;
            audioDto.userNames = audio.userNames;

        }

        return Results.json().render(audioDto);

    }


    public Result getAudiosJson() {
        
        AudiosDto audiosDto = audioDao.getAllAudios();
        
        return Results.json().render(audiosDto);
        
    }
    
    public Result getAudiosXml() {
        
        AudiosDto audiosDto = audioDao.getAllAudios();
        
        return Results.xml().render(audiosDto);
        
    }


    public Result getAudiosJsonFromUser(@LoggedInUser String username) {

        AudiosDto audiosDto = audioDao.getAllAudiosFromUser(username);

        return Results.json().render(audiosDto);

    }

    public Result getAudiosXmlFromUser(@LoggedInUser String username) {

        AudiosDto audiosDto = audioDao.getAllAudiosFromUser(username);

        return Results.xml().render(audiosDto);

    }

    @FilterWith(SecureFilter.class)
    public Result uploadAudioJson(@LoggedInUser String username,
                                  AudioDto audioDto) {

        boolean succeeded = audioDao.uploadAudio(username, audioDto);

        if (!succeeded) {
            return Results.notFound();
        } else {
            return Results.json();
        }

    }

    @FilterWith(SecureFilter.class)
    public Result uploadAudioXml(@LoggedInUser String username,
                                 AudioDto audioDto) {

        boolean succeeded = audioDao.uploadAudio(username, audioDto);

        if (!succeeded) {
            return Results.notFound();
        } else {
            return Results.xml();
        }

    }
}
