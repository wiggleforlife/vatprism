package net.marvk.fs.vatsim.map.discord;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.data.Preferences;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
//TODO organise imports

@Log4j2
@Singleton
public class DiscordClient
{
    private static final String PREFERENCE_KEY = "general.discord_rich_presence";

    private final Preferences prefs;

    @Inject()
    public DiscordClient(final Preferences preferences) {
        this.prefs = preferences;
    }

    //TODO add function desc
    public void start()
    {
        if(!this.prefs.booleanProperty(DiscordClient.PREFERENCE_KEY).get()) {
            return;
        }

        try {
            Core.initDownload();
        } catch (IOException e) {
            e.printStackTrace(); //TODO better logging
        }

        // Set parameters for the Core
        try(CreateParams params = new CreateParams())
        {
            params.setClientID(955362289949761547L); //TODO replace with a production version
            params.setFlags(CreateParams.getDefaultFlags());
            // Create the Core
            try(Core core = new Core(params))
            {
                // Create the Activity
                try(Activity activity = new Activity())
                {
                    activity.setDetails("DEV TEST");
                    activity.setState("Idle");

                    // Setting a start time causes an "elapsed" field to appear
                    activity.timestamps().setStart(Instant.now());

                    // Show logo with tooltip
                    activity.assets().setLargeImage("logo");
                    activity.assets().setLargeText("VATPrism");

                    // Finally, update the current activity to our activity
                    core.activityManager().updateActivity(activity);
                }

                // Run callbacks forever
                while(true)
                {
                    core.runCallbacks();
                    try
                    {
                        // Sleep a bit to save CPU
                        Thread.sleep(16);
                    }
                    catch(InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
