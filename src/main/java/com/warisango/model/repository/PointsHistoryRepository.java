package com.warisango.model.repository;
import com.google.firebase.cloud.FirestoreClient; import com.warisango.model.PointsHistory; import org.springframework.stereotype.Repository; import java.util.List;
@Repository public class PointsHistoryRepository { public List<PointsHistory> findByUserId(String uid){try{return FirestoreClient.getFirestore().collection("pointsHistory").whereEqualTo("userId",uid).orderBy("occurredAt").get().get().toObjects(PointsHistory.class);}catch(Exception e){return List.of();}}}
