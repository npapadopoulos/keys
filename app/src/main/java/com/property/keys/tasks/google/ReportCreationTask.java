package com.property.keys.tasks.google;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.util.Pair;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.firebase.database.DataSnapshot;
import com.property.keys.R;
import com.property.keys.entities.HistoryDetails;
import com.property.keys.tasks.AbstractAsyncTask;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;

import static com.property.keys.utils.Utils.DATE_TIME_FORMATTER;
import static java.util.Collections.singletonList;

@RequiresApi(api = Build.VERSION_CODES.R)
@AllArgsConstructor
@Builder
public class ReportCreationTask extends AbstractAsyncTask {
    public static final String APPLICATION_NAME = "Keys";
    public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TAG = ReportCreationTask.class.getSimpleName();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private final String fileName;
    private final Activity activity;
    private final DataSnapshot dataSnapshot;
    private final long from;
    private final long to;
    private final Consumer<Pair<LocalDateTime, LocalDateTime>> onEmptyResult;

    @SneakyThrows
    @Override
    public Void doInBackground(Void... voids) {
        LocalDateTime fromDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(from), ZoneId.systemDefault());
        LocalDateTime toDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(to), ZoneId.systemDefault());
        Spreadsheet spreadsheet = new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle(fileName));
        List<ValueRange> ranges = new ArrayList<>();
        ranges.add(new ValueRange()
                .setRange("A1:I1")
                .setValues(singletonList(
                        Arrays.asList(
                                "First Name",
                                "Last Name",
                                "Property Name",
                                "Key Purpose",
                                "Key Location",
                                "Key Check In Reason",
                                "Key Check In Date",
                                "Key Estimated Check Out Date",
                                "Key Check Out Date"
                        )
                )));

        AtomicInteger index = new AtomicInteger(2);
        dataSnapshot.getChildren().forEach(child -> {
            HistoryDetails historyDetails = child.getValue(HistoryDetails.class);

            LocalDateTime created = LocalDateTime.from(DATE_TIME_FORMATTER.parse(historyDetails.getCreated()));
            if (created.isAfter(fromDate) && created.isBefore(toDate)) {
                int i = index.getAndIncrement();
                Object[] values = new Object[9];
                values[0] = historyDetails.getFirstName();
                values[1] = historyDetails.getLastName();
                values[2] = historyDetails.getKey().getPropertyName();
                values[3] = historyDetails.getKey().getPurpose();
                values[4] = historyDetails.getKey().getLocation();
                values[5] = historyDetails.getKey().getCheckInReason();
                values[6] = historyDetails.getKey().getCheckedInDate();
                values[7] = historyDetails.getKey().getEstimatedCheckOutDate();
                values[8] = historyDetails.getKey().getCheckedOutDate();
                ranges.add(new ValueRange()
                        .setRange("A" + i + ":I" + i)
                        .setValues(singletonList(Arrays.asList(values))));
            }
        });

        if (ranges.size() > 1) {
            final NetHttpTransport httpTransport = new com.google.api.client.http.javanet.NetHttpTransport();
            Sheets sheets = new Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials())
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            spreadsheet = sheets.spreadsheets().create(spreadsheet)
                    .setFields("spreadsheetId")
                    .execute();

            BatchUpdateValuesResponse response = sheets.spreadsheets().values().batchUpdate(spreadsheet.getSpreadsheetId(), new BatchUpdateValuesRequest().setValueInputOption("USER_ENTERED").setData(ranges))
                    .execute();//TODO change to queue in order to add callback and inform user that the report has been generated
            JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
                @Override
                public void onFailure(GoogleJsonError e,
                                      HttpHeaders responseHeaders)
                        throws IOException {
                }

                @Override
                public void onSuccess(Permission permission,
                                      HttpHeaders responseHeaders)
                        throws IOException {
                }
            };
            Drive drive = new Drive.Builder(httpTransport, JSON_FACTORY, getCredentials())
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            BatchRequest batch = drive.batch();
            Permission userPermission = new Permission()
                    .setType("user")
                    .setRole("writer")
                    .setEmailAddress("n2.papadopoulos@gmail.com"); //TODO change to application account email when created as well as firebase account
            drive.permissions().create(response.getSpreadsheetId(), userPermission)
                    .setFields("id")
                    .queue(batch, callback);

            batch.execute();
        } else {
            onEmptyResult.accept(new Pair<>(fromDate, toDate));
        }
        return null;
    }

    @SneakyThrows
    private Credential getCredentials() {
        return GoogleCredential.fromStream(activity.getResources().openRawResource(R.raw.credentials))
                .createScoped(SheetsScopes.all());
    }
}
