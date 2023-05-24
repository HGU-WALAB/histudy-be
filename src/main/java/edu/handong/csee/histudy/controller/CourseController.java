package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/course")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    @PostMapping
    public ResponseEntity<String> importCourses(@RequestParam("file")MultipartFile file) {
        if(file.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_ACCEPTABLE)
                    .body("Empty file");
        }
        else {
            String status = courseService.uploadFile(file);
            if(status.equals("FAILED"))
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed in server");
            else
                return ResponseEntity.ok(status);
        }
    }
}