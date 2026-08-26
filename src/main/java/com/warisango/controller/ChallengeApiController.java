package com.warisango.controller;

import com.warisango.model.service.ChallengeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class ChallengeApiController {
    private final ChallengeService service;
    public ChallengeApiController(ChallengeService service) { this.service = service; }

    @GetMapping("/api/challenges")
    public ResponseEntity<?> list(@RequestParam(defaultValue="demo-user") String userId) {
        try { return ResponseEntity.ok(service.getChallenges(userId)); }
        catch (Exception e) { return ResponseEntity.internalServerError().body("Unable to load challenges."); }
    }

    @PostMapping("/api/challenges/{id}/join")
    public ResponseEntity<?> join(@PathVariable String id, @RequestParam(defaultValue="demo-user") String userId) {
        try { service.join(userId,id); return ResponseEntity.ok(Map.of("success",true)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("success",false,"message",e.getMessage())); }
    }

    @PostMapping("/api/challenges/{id}/claim")
    public ResponseEntity<?> claim(@PathVariable String id, @RequestParam(defaultValue="demo-user") String userId) {
        try { return ResponseEntity.ok(service.claim(userId,id)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("success",false,"message",e.getMessage())); }
    }

    @GetMapping("/api/admin/challenges")
    public ResponseEntity<?> adminList() { try { return ResponseEntity.ok(service.adminList()); } catch(Exception e){return ResponseEntity.internalServerError().build();} }

    @PostMapping("/api/admin/challenges")
    public ResponseEntity<?> create(@RequestBody Map<String,Object> body) { try { return ResponseEntity.ok(Map.of("id",service.create(body))); } catch(Exception e){return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));} }

    @PutMapping("/api/admin/challenges/{id}")
    public ResponseEntity<?> update(@PathVariable String id,@RequestBody Map<String,Object> body) { try { service.update(id,body); return ResponseEntity.ok(Map.of("success",true)); } catch(Exception e){return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));} }

    @DeleteMapping("/api/admin/challenges/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) { try { service.delete(id); return ResponseEntity.ok(Map.of("success",true)); } catch(Exception e){return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));} }
}
