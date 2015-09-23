package com.maciekjanusz.compassproject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;
import java.util.Random;

import static com.maciekjanusz.compassproject.util.CompassMath.MAX_LATITUDE;
import static com.maciekjanusz.compassproject.util.CompassMath.MAX_LONGITUDE;
import static com.maciekjanusz.compassproject.util.CompassMath.calculateBearing;
import static com.maciekjanusz.compassproject.util.CompassMath.validateLatitude;
import static com.maciekjanusz.compassproject.util.CompassMath.validateLongitude;
import static com.maciekjanusz.compassproject.util.CompassMath.decimalToDegrees;
import static com.maciekjanusz.compassproject.util.CompassMath.degreesToDecimal;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@Config(constants = BuildConfig.class, sdk = 21,
        manifest = "app/src/main/AndroidManifest.xml")
@RunWith(RobolectricGradleTestRunner.class)
public class CompassMathTest {

    @Test
    public void testCalculateBearing() throws Exception {
        float lat1 = 0f, lon1 = 0f, lat2 = 0f, lon2 = 0f;
        float bearing = (float) calculateBearing(lat1, lon1, lat2, lon2);
        assertThat(bearing, equalTo(0f));

        lon2 = 90f;
        bearing = (float) calculateBearing(lat1, lon1, lat2, lon2);
        assertThat(bearing, equalTo(90f));

        lat2 = 45f;
        bearing = (float) calculateBearing(lat1, lon1, lat2, lon2);
        assertThat(bearing, equalTo(45f));

        lat2 = -45f;
        lon2 = 0f;
        bearing = (float) calculateBearing(lat1, lon1, lat2, lon2);
        assertThat(bearing, equalTo(180f));
    }

    @Test
    public void testValidateLatitude() throws Exception {
        Random random = new Random();
        float interval = 2 * MAX_LATITUDE;
        float randomRightLatitude = (random.nextFloat() * interval) - MAX_LATITUDE;
        float randomWrongLatitude = (random.nextFloat() * interval) + MAX_LATITUDE + 0.1f;

        assertThat(validateLatitude(randomRightLatitude), equalTo(true));
        assertThat(validateLatitude(randomWrongLatitude), equalTo(false));
        assertThat(validateLatitude(MAX_LATITUDE), equalTo(true));
        assertThat(validateLatitude(-MAX_LATITUDE), equalTo(true));
    }

    @Test
    public void testValidateLongitude() throws Exception {
        Random random = new Random();
        float interval = 2 * MAX_LONGITUDE;
        float randomRightLatitude = (random.nextFloat() * interval) - MAX_LONGITUDE;
        float randomWrongLatitude = (random.nextFloat() * interval) + MAX_LONGITUDE + 0.1f;

        assertThat(validateLongitude(randomRightLatitude), equalTo(true));
        assertThat(validateLongitude(randomWrongLatitude), equalTo(false));
        assertThat(validateLongitude(MAX_LONGITUDE), equalTo(true));
        assertThat(validateLongitude(-MAX_LONGITUDE), equalTo(true));
    }

    @Test
    public void testCoordinateConversion() throws Exception {
        float decimal = 50.06f;
        float[] degMinSec = decimalToDegrees(decimal);
        float resultDecimal = degreesToDecimal((int)degMinSec[0], (int)degMinSec[1], degMinSec[2]);
        resultDecimal = round(resultDecimal, 2);

        assertThat(resultDecimal, equalTo(decimal));

        // test with 0 value
        decimal = 0;
        degMinSec = decimalToDegrees(decimal);
        resultDecimal = degreesToDecimal((int)degMinSec[0], (int)degMinSec[1], degMinSec[2]);
        resultDecimal = round(resultDecimal, 2);

        assertThat(resultDecimal, equalTo(decimal));

        // test with more decimal places
        decimal = 39.572141f;
        degMinSec = decimalToDegrees(decimal);
        resultDecimal = degreesToDecimal((int)degMinSec[0], (int)degMinSec[1], degMinSec[2]);
        resultDecimal = round(resultDecimal, 6);

        assertThat(resultDecimal, equalTo(decimal));
    }

    public static float round(float d, int decimalPlaces) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}