package no.kristiania.lifeos.user;

import java.util.Locale;

public final class Emails {

    private Emails() {}

    public static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
