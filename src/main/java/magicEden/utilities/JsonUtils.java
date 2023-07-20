package magicEden.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import magicEden.json.Account;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON utility methods to help parse and process JSON files
 */
public class JsonUtils {
    public ArrayList<Account> loadJsonAccountFile(String jsonFileName) {
        // Read JSON from coding-challenge-input which simulates various account types
        // being ingested
        final Gson gson = new GsonBuilder().setDateFormat("SSSS").create();
        final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(jsonFileName);
        assert resourceAsStream != null;

        final BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
        return gson.fromJson(br, new TypeToken<List<Account>>() {}.getType());
    }
}
