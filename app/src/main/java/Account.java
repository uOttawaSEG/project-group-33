public abstract class Account {
    private String firstName, lastName, email, password, phone = "", type; // instance variables

    // default constructor when super() is called
    Account(String firstName, String lastName, String email, String password,  String phone){
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.setEmail(email);
        this.setPhone(phone);
    }

    //setters:
    public void setFirstName(String s){
        this.firstName = s;
    } public void setLastName(String s) {
        this.lastName = s;
    } public void setEmail(String s){
        this.email = s;
    } public void setPassword(String s){
        this.password = s;
    }

    public void setPhone(String s){ // Sets the phone number, removing non-numeric characters
        phone = s.replaceAll("\\D", ""); // replace all digits ("\\D" is regex for NON digits) with ""
    }

    // getters
    public String getFirstName() {
        return firstName;
    } public String getLastName(){
        return lastName;
    } public String getEmail(){
        return email;
    } public String getPassword(){
        return password;
    } public String getPhone(){
        return phone;
    } public String getType(){
        return type;
    }

}
