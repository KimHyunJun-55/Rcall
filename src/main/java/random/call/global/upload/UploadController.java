package random.call.global.upload;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;


    @PostMapping("")
    public ResponseEntity<String> upload(MultipartFile file) throws IOException {
        String uploadUrl = uploadService.upload(file);
       return ResponseEntity.ok(uploadUrl);
    }
}
