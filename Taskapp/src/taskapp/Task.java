package taskapp;

/**
* Represents a Task in the system.
* NOTE: Added 'progress' field as requested (0-100).
*/
public class Task {
   private int id;
   private String title;
   private String description;
   private String priority; // High, Medium, Low
   private int assignedToUserId;
   private String assignedToUsername;
   private boolean isComplete;
   private int progress; // New field for task progress (0-100)

   public Task(int id, String title, String description, String priority, int assignedToUserId, String assignedToUsername, boolean isComplete, int progress) {
       this.id = id;
       this.title = title;
       this.description = description;
       this.priority = priority;
       this.assignedToUserId = assignedToUserId;
       this.assignedToUsername = assignedToUsername;
       this.isComplete = isComplete;
       this.progress = progress;
   }

   // Getters
   public int getId() { return id; }
   public String getTitle() { return title; }
   public String getDescription() { return description; }
   public String getPriority() { return priority; }
   public int getAssignedToUserId() { return assignedToUserId; }
   public String getAssignedToUsername() { return assignedToUsername; }
   public boolean isComplete() { return isComplete; }
   public int getProgress() { return progress; } // New Getter

   // Setters for editing
   public void setTitle(String title) { this.title = title; }
   public void setDescription(String description) { this.description = description; }
   public void setPriority(String priority) { this.priority = priority; }
   public void setAssignedToUserId(int assignedToUserId) { this.assignedToUserId = assignedToUserId; }
   public void setAssignedToUsername(String assignedToUsername) { this.assignedToUsername = assignedToUsername; }
   public void setComplete(boolean complete) { isComplete = complete; }
   public void setProgress(int progress) { this.progress = progress; } // New Setter

   @Override
   public String toString() {
       String status = isComplete ? "[COMPLETE]" : "[PENDING]";
       return String.format("%s ID: %d | %s | Priority: %s | Progress: %d%% | Assigned to: %s",
           status, id, title, priority, progress, assignedToUsername);
   }
}
