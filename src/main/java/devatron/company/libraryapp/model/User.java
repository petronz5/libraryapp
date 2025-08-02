package devatron.company.libraryapp.model;

public class User {
    private int id;
    private String email;
    private String password;
    private String ruolo;

    public User(int id, String email, String password , String ruolo) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.ruolo = ruolo;
    }

    public User(String email, String password, String ruolo) {
        this.email = email;
        this.password = password;
        this.ruolo = ruolo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRuolo() { return ruolo; }
    public void setRuolo(String ruolo) { this.ruolo = ruolo; }
}
