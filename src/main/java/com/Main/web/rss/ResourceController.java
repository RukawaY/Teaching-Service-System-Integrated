package com.Main.web.rss;

import com.Main.config.ResourceAppConfig;
import com.Main.service.rss.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "*")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ResourceAppConfig resourceAppConfig;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("directoryId") Long directoryId,
            @RequestParam("courseId") Long courseId,
            @RequestParam("ownerId") Long ownerId,
            @RequestParam(value = "resourceName", required = false) String resourceName,
            @RequestParam(value = "description", required = false) String description) {
        return resourceService.uploadFile(file, directoryId, courseId, ownerId, resourceName, description);
    }

    @GetMapping("/download/{resourceId}")
    public ResponseEntity<?> downloadFile(@PathVariable Long resourceId) {
        return resourceService.downloadFile(resourceId);
    }

    @GetMapping("/directory/{courseId}")
    public ResponseEntity<?> getDirectory(@PathVariable String courseId) {
        return resourceService.getDirectory(courseId);
    }
}
