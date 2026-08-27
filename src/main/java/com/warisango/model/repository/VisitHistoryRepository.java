package com.warisango.model.repository;
import com.google.firebase.cloud.FirestoreClient; import com.warisango.model.VisitHistory; import org.springframework.stereotype.Repository; import java.util.List;
@Repository public class VisitHistoryRepository { public List<VisitHistory> findByUserId(String uid){try{return FirestoreClient.getFirestore().collection("visitHistory").whereEqualTo("userId",uid).orderBy("visitedAt").get().get().toObjects(VisitHistory.class);}catch(Exception e){return List.of();}}}
