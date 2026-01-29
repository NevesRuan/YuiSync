package com.yuisync.service;

import java.io.File;

public interface VideoPlatform {
    boolean supports(String url);
    File download(String url);
}
