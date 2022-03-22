package net.marvk.fs.vatsim.map.discord;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.jcm.discordgamesdk.ActivityManager;
import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import java.io.IOException;
import java.time.Instant;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.data.Preferences;

@Log4j2
@Singleton
public class DiscordClient
{
    private static final String PREFERENCE_KEY = "general.discord_rich_presence";

    private final Preferences prefs;
    private boolean running = false;
    private Instant startTime;
    private ActivityManager activityManager;

    @Inject()
    public DiscordClient(final Preferences preferences) {
        this.prefs = preferences;
    }

    //TODO add function desc
    public void start() {
        // Check if RPC is enabled
        if(!this.prefs.booleanProperty(DiscordClient.PREFERENCE_KEY).get()) {
            return;
        }

        try {
            Core.initDownload();
        } catch (IOException e) {
            log.error("Discord Native Library download failed");
            log.error(e.getStackTrace());
        }

        // Set parameters for the Core
        try(CreateParams params = new CreateParams()) {
            params.setClientID(955362289949761547L); //TODO replace with a production version
            params.setFlags(CreateParams.getDefaultFlags());
            // Create the Core
            try(Core core = new Core(params)) {
                // Create the Activity
                this.activityManager = core.activityManager();
                startTime = Instant.now();
                useActivityTemplate("idle");
                running = true;

                // Run callbacks forever
                log.info("Starting RPC callback");
                while(running) {
                    core.runCallbacks();
                    try {
                        // Sleep a bit to save CPU
                        Thread.sleep(16);
                    }
                    catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void updateActivity(String details, String state) {
        if (!this.running) {
            log.error("RPC isn't running");
        }

        try (Activity activity = new Activity()) {
            // Create description field
            activity.setDetails(details);
            activity.setState(state);
            // Create elapsed time field
            activity.timestamps().setStart(startTime);
            // Create logo
            activity.assets().setLargeImage("logo");
            activity.assets().setLargeText("VATPrism");

            // Update the activity
            this.activityManager.updateActivity(activity);
            log.info("Updated RPC");
        }
    }

    public void useActivityTemplate(String template) {
        switch (template) {
            case "idle":
                updateActivity("", "Idle");
                break;
        }
    }
}
