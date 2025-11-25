package com.example.SecuroServBackend.Controllers;

import com.example.SecuroServBackend.Service.FileEntityServices;
import com.example.SecuroServBackend.Service.FolderServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/FolderRelated")
public class FolderController {
    @Autowired
    private FolderServices folderServices;
    @PostMapping("/singleFile")
    public ResponseEntity<String> uploadFiles(@RequestParam("files") List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body("No files provided.");
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File " + file.getOriginalFilename() + " is empty.");
            }
        }


        return ResponseEntity.ok("Uploaded successfully");
    }
}
