package com.parsons.bakery;


import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateHelper {
    private static final String TAG = "FIREBASE";

    public static void PullMenu(final FirebaseFirestore db, final Callback<List<Map<String, Object>>> callback) {
        // Initialize the returned object
        List<Map<String, Object>> returns = new ArrayList<>();

        // Get reference to the reference sheet
        DocumentReference ref = db.collection("categories").document("categoryReferences");

        // Fetch the main reference sheet asynchronously
        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot mainDocument = task.getResult();

                if (mainDocument.exists() && mainDocument.getData() != null && !mainDocument.getData().isEmpty()) {
                    // List to hold all category tasks
                    List<Task<Void>> categoryTasks = new ArrayList<>();

                    for (Map.Entry<String, Object> entry : mainDocument.getData().entrySet()) {
                        if (entry.getValue() instanceof DocumentReference) {
                            DocumentReference categoryRef = (DocumentReference) entry.getValue();

                            // Fetch category reference and chain sub-tasks to it
                            Task<Void> categoryTask = categoryRef.get().continueWithTask(categoryTaskResult -> {
                                if (categoryTaskResult.isSuccessful()) {
                                    DocumentSnapshot categorySnapshot = categoryTaskResult.getResult();
                                    Map<String, Map<String, Object>> documents = new HashMap<>();
                                    List<Task<DocumentSnapshot>> subTasks = new ArrayList<>();

                                    // Cycle through all information for each category
                                    for (Map.Entry<String, Object> item : categorySnapshot.getData().entrySet()) {
                                        if (item.getValue() instanceof DocumentReference) {
                                            DocumentReference itemRef = (DocumentReference) item.getValue();

                                            // Fetch sub-document
                                            Task<DocumentSnapshot> subTask = itemRef.get();
                                            subTasks.add(subTask);

                                            subTask.addOnCompleteListener(subTaskResult -> {
                                                if (subTaskResult.isSuccessful()) {
                                                    documents.put(item.getKey(), subTaskResult.getResult().getData());
                                                } else {
                                                    System.out.println("Failed to pull document reference: " + itemRef.getPath());
                                                }
                                            });
                                        } else {
                                            // Store non-document references directly
                                            Map<String, Object> newMap = new HashMap<>();
                                            newMap.put("0", item.getValue());
                                            documents.put(item.getKey(), newMap);
                                        }
                                    }

                                    // Return a task that waits for all subTasks to complete
                                    return Tasks.whenAllComplete(subTasks).continueWith(subTaskCompletion -> {
                                        if (subTaskCompletion.isSuccessful()) {
                                            // Process documents after all sub-tasks are done
                                            for (Map.Entry<String, Object> item : documents.get("name").entrySet()) {
                                                String currentName = (String) item.getValue();
                                                //System.out.println(currentName + ": " + j);
                                                String category = entry.getKey();
                                                String description = (String) documents.get("description").get(item.getKey());
                                                String innerCategory = "";
                                                String order = item.getKey();
                                                String req = "";
                                                Long price = (Long) documents.get("price").get(item.getKey());
                                                Long dozenPrice = (Long) documents.get("dozenPrice").get(item.getKey());
                                                boolean useReq = true;
                                                boolean useInner = false;
                                                try {
                                                    req = (String) documents.get("req").get(item.getKey());
         g                                       } catch (NullPointerException e) {
                                                    useReq = false;
                                                }
                                                String image = "null";
                                                try {
                                                    image = (String) documents.get("images").get(item.getKey());
                                                } catch (NullPointerException ignored) {
                                                }

                                                if ((boolean) documents.get("hasLevels").get("0")) {
                                                    innerCategory = (String) documents.get("innerCategory").get(item.getKey());
                                                    useInner = (boolean) documents.get("useInner").get(item.getKey());
                                                }

                                                String finalInnerCategory = innerCategory;
                                                boolean finalUseInner = useInner;
                                                String finalImage = image;
                                                HashMap<String, Object> row = new HashMap<String, Object>() {{
                                                    put("name", currentName);
                                                    put("category", category);
                                                    put("description", description);
                                                    put("innerCategory", finalInnerCategory);
                                                    put("useInner", finalUseInner);
                                                    put("order_of_options", order);
                                                    put("image", finalImage);
                                                    put("price", price);
                                                    put("dozenPrice", dozenPrice);
                                                }};
                                                if (useReq) {
                                                    row.put("req", req);
                                                }
                                                returns.add(row);
                                            }
                                        } else {
                                            System.out.println("One or more sub-document fetch tasks failed.");
                                        }
                                        return null;
                                    });
                                } else {
                                    System.out.println("Couldn't pull category reference sheet!");
                                    return Tasks.forResult(null);  // Return a completed task
                                }
                            });

                            // Add the chained categoryTask to the list
                            categoryTasks.add(categoryTask);
                        } else {
                            System.out.println("ERROR IN REFERENCES");
                        }
                    }

                    // When all category tasks (and their sub-tasks) are complete, trigger the callback
                    Tasks.whenAllComplete(categoryTasks).addOnCompleteListener(categoryCompletionTask -> {
                        if (categoryCompletionTask.isSuccessful()) {
                            System.out.println("All category reference sheets fetched successfully.");
                            // Call the callback and pass the results
                            callback.onComplete(returns);
                        } else {
                            System.out.println("One or more category fetch tasks failed.");
                            callback.onFailure(new Exception("One or more category fetch tasks failed."));  // Can handle error cases as well
                        }
                    });
                } else {
                    System.out.println("Main Document is empty, null, or non-existent");
                    callback.onFailure(new Exception("Main Document is empty, null, or non-existent"));
                }

            } else {
                System.out.println("Couldn't pull main reference sheet!");
                // Call the callback with an empty list or null on failure
                callback.onFailure(new Exception("ERROR: NO REFERENCE PULLED"));
            }
        });
    }

    public static void PullCategories(FirebaseFirestore db, Callback<List<Map<String, Object>>> pullCategoriesCallback) {

        List<Map<String, Object>> returns = new ArrayList<>();

        DocumentReference ref = db.collection("categories").document("categoryReferences");

        Task<DocumentSnapshot> mainRef = ref.get();

        mainRef.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

                for (Map.Entry<String, Object> entry : task.getResult().getData().entrySet()) {
                    DocumentReference categoryRef = db.document(((DocumentReference) entry.getValue()).getPath());
                    Task<DocumentSnapshot> docTask = categoryRef.get();
                    tasks.add(docTask);

                    docTask.addOnCompleteListener(categoryTask -> {
                        if (categoryTask.isSuccessful()) {
                            Map<String, Object> info = categoryTask.getResult().getData();
                            info.put("entry", entry.getKey());
                            returns.add(info);
                        } else {
                            System.out.println("Couldn't retrieve the category reference sheet: " + categoryRef.getPath());
                        }
                    });
                }
                Tasks.whenAllComplete(tasks).addOnCompleteListener(listener -> {
                    pullCategoriesCallback.onComplete(returns);
                });
            } else {
                pullCategoriesCallback.onComplete(returns);
                System.out.println("get failed with " + task.getException().toString());
            }
        });
    }

    public static void PullCustomizations(FirebaseFirestore db, Callback<List<Map<String, Object>>> callback) {
        List<Map<String, Object>> returns = new ArrayList<>();
        db.collection("customizations").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        returns.add(snapshot.getData());
                    }
                }
                callback.onComplete(returns);
            } else {
                callback.onFailure(new Exception("Failure in completing call to collection"));
            }
        });
    }

    public static void PullOrders(FirebaseFirestore db, Callback<List<Map<String, Object>>> callback) {
        List<Map<String, Object>> returns = new ArrayList<>();

        db.collection("orders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                if (!task.getResult().isEmpty()) {
                    List<Task<QuerySnapshot>> subcollectionTasks = new ArrayList<>();

                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        // Create a map for the main document data
                        Map<String, Object> orderData = new HashMap<>(snapshot.getData());

                        // Use the document ID as the unique identifier
                        String documentId = snapshot.getId(); // Get the Firestore document ID
                        orderData.put("documentId", documentId);


                        // Fetch the "orderSpecifications" subcollection for this document
                        Task<QuerySnapshot> subcollectionTask = snapshot.getReference()
                                .collection("orderSpecifications")
                                .get()
                                .continueWith(subTask -> {
                                    if (subTask.isSuccessful() && subTask.getResult() != null) {
                                        List<Map<String, Object>> specs = new ArrayList<>();
                                        for (QueryDocumentSnapshot specDoc : subTask.getResult()) {
                                            specs.add(specDoc.getData()); // Add subdocument data
                                        }
                                        orderData.put("orderSpecifications", specs); // Add the subcollection data
                                    } else {
                                        System.err.println("Failed to fetch subcollection for order: " + documentId);
                                    }
                                    synchronized (returns) {
                                        returns.add(orderData);
                                    }
                                    return null;
                                });

                        subcollectionTasks.add(subcollectionTask); // Track the task
                    }

                    // Wait for all subcollection queries to complete
                    Tasks.whenAllComplete(subcollectionTasks).addOnCompleteListener(ignored -> {
                        callback.onComplete(returns);
                    });

                } else {
                    callback.onComplete(returns); // No documents in "orders"
                }
            } else {
                callback.onFailure(new Exception("Failure in completing call to collection"));
            }
        });
    }



    public static class ProcessTaskWrapper {

        public static Task<List<Map<String, Object>>> wrapPullMenuProcess(final FirebaseFirestore db) {
            TaskCompletionSource<List<Map<String, Object>>> taskCompletionSource = new TaskCompletionSource<>();

            // Start the PullMenu process and ensure the task is only completed when all async work is done
            PullMenu(db, new Callback<List<Map<String, Object>>>() {
                @Override
                public void onComplete(List<Map<String, Object>> result) {
                    taskCompletionSource.setResult(result);  // Complete task with the result
                }

                @Override
                public void onFailure(Exception e) {
                    taskCompletionSource.setException(e);  // Complete task with an error if something fails
                }
            });

            // Return the Task object so it can be managed like a Firestore task
            return taskCompletionSource.getTask();
        }
        public static Task<List<Map<String, Object>>> wrapPullCustomizationsProcess(final FirebaseFirestore db) {
            TaskCompletionSource<List<Map<String, Object>>> taskCompletionSource = new TaskCompletionSource<>();

            // Start the PullMenu process and ensure the task is only completed when all async work is done
            PullCustomizations(db, new Callback<List<Map<String, Object>>>() {
                @Override
                public void onComplete(List<Map<String, Object>> result) {
                    taskCompletionSource.setResult(result);  // Complete task with the result
                }

                @Override
                public void onFailure(Exception e) {
                    taskCompletionSource.setException(e);  // Complete task with an error if something fails
                }
            });

            // Return the Task object so it can be managed like a Firestore task
            return taskCompletionSource.getTask();
        }


        public static Task<List<Map<String, Object>>> wrapPullOrdersProcess(final FirebaseFirestore db) {
            TaskCompletionSource<List<Map<String, Object>>> taskCompletionSource = new TaskCompletionSource<>();

            // Start the PullMenu process and ensure the task is only completed when all async work is done
            PullOrders(db, new Callback<List<Map<String, Object>>>() {
                @Override
                public void onComplete(List<Map<String, Object>> result) {
                    taskCompletionSource.setResult(result);  // Complete task with the result
                }

                @Override
                public void onFailure(Exception e) {
                    taskCompletionSource.setException(e);  // Complete task with an error if something fails
                }
            });

            // Return the Task object so it can be managed like a Firestore task
            return taskCompletionSource.getTask();
        }

        public static Task<List<Map<String, Object>>> wrapPullCategories(final FirebaseFirestore db) {
            TaskCompletionSource<List<Map<String, Object>>> taskCompletionSource = new TaskCompletionSource<>();

            PullCategories(db, new Callback<List<Map<String, Object>>>() {
                @Override
                public void onComplete(List<Map<String, Object>> result) {
                    taskCompletionSource.setResult(result);  // Complete task with the result
                }

                @Override
                public void onFailure(Exception e) {
                    taskCompletionSource.setException(e);  // Complete task with an error if something fails
                }
            });

            return taskCompletionSource.getTask();
        }
    }


    // Define a callback interface for both success and failure
    public interface Callback<T> {
        void onComplete(T result);     // Called when the operation completes successfully
        void onFailure(Exception e);   // Called when there's an error
    }

}
