package com.taskmanagement.servlet;

import com.google.gson.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Base abstract servlet providing standardized JSON responses,
 * error handling, and LocalDate Gson serialization.
 */
public abstract class BaseServlet extends HttpServlet {

    protected static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
                @Override
                public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE));
                }
            })
            .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
                @Override
                public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                    if (json == null || json.getAsString().trim().isEmpty()) {
                        return null;
                    }
                    return LocalDate.parse(json.getAsString().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                }
            })
            .create();

    protected void writeSuccess(HttpServletResponse resp, Object data, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);

        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("message", message != null ? message : "Operation successful");
        map.put("data", data);

        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(map));
        out.flush();
    }

    protected void writeError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(statusCode);

        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("error", message);

        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(map));
        out.flush();
    }

    protected String readRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
