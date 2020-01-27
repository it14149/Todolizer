package gr.edu.todolizer;

public class Events {
 
    static class DeletedTaskID {
        private int position;
 
         DeletedTaskID(int position) {
            this.position = position;
        }
         int getPosition(){
             return position;
        }
    }
 

    public static class CheckboxStatus{

        private int position;
        private boolean isChecked;

        public CheckboxStatus(int position, boolean isChecked){
            this.position = position;
            this.isChecked = isChecked;
        }

        public int getPosition(){
            return position;
        }

        public boolean getChecked(){
            return isChecked;
        }
    }


    public static class DescriptionChanged {
        private int position;
        private String description;
        public DescriptionChanged(int position, String changedDescription) {
            this.position = position;
            this.description = changedDescription;
        }

        public int getPosition(){
            return position;
        }

        public String getDescription(){
            return description;
        }
    }


    public static class TitleChanged {
        private int position;
        private String title;
        public TitleChanged(int position, String changedTitle) {
            this.position = position;
            this.title = changedTitle;
        }

        public int getPosition(){
            return position;
        }

        public String getTitle(){
            return title;
        }
    }


}