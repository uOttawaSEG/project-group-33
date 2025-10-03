public class Tutor extends Account{
    // Attributes for TUTORS:
    private String education;
    private String[] courses;

    // Constructor
    Tutor(String firstName, String lastName, String email, String password,  String phone){
        super(firstName, lastName, email, password, phone); // call super for Account constructor
    }
    // Setters
    public void setEducation(String education) {
        this.education = education;
    }
    public void setCourses(String[] courses){
        this.courses = courses;
    }

    // Getters for tutor-specific variables
    public String getEducation() {
        return education;
    }
    public String[] getCourses(){
        return courses;
    }
}
