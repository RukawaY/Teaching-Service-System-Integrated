package com.Main.web.rss;

import com.Main.service.rss.impl.ResourceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    @Autowired
    private ResourceServiceImpl resourceService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("directoryId") Long directoryId,
            @RequestParam("courseId") Long courseId,
            @RequestParam("ownerId") Long ownerId,
            @RequestParam(value = "resource_name", required = false) String resourceName,
            @RequestParam(value = "description", required = false) String description) {
        return resourceService.uploadFile(file, directoryId, courseId, ownerId, resourceName, description);
    }

    @GetMapping("/download/{resourceId}")
    public ResponseEntity<?> downloadFile(@PathVariable Long resourceId) {
        return resourceService.downloadFile(resourceId);
    }

    @GetMapping("/directory/{courseId}")
    public ResponseEntity<?> getDirectory(@PathVariable Long courseId) {
        return resourceService.getDirectory(courseId);
    }
}
