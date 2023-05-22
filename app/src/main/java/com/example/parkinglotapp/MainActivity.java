package com.example.parkinglotapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.ViewGroup;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.google.android.material.snackbar.Snackbar;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.CalloutBalloonAdapter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener {
    private MapView mapView;
    private ViewGroup mapViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("키해시는 :", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // 권한ID를 가져옵니다
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);

        int permission2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        int permission3 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        // 권한이 열려있는지 확인
        if (permission == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED || permission3 == PackageManager.PERMISSION_DENIED) {
            // 마쉬멜로우 이상버전부터 권한을 물어본다
            if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 권한 체크(READ_PHONE_STATE의 requestCode를 1000으로 세팅
                requestPermissions(
                        new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        1000);
            }
            return;
        }

        //지도를 띄우자
        // java code
        mapView = new MapView(this);

        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);


        // 줌 레벨 변경
        mapView.setZoomLevel(4, true);

        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);

        //구현한 CalloutBalloonAdapter 등록
        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());

        createCustomMarker(mapView);


    }

    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter{
        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter(){
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem){
            ((ImageView) mCalloutBalloon.findViewById(R.id.badge)).setImageResource(R.drawable.parking_place_transportation_road_area_icon_228898);
            ((TextView) mCalloutBalloon.findViewById(R.id.title)).setText(poiItem.getItemName());
            HashMap<String, String> userObject = (HashMap<String, String>)poiItem.getUserObject();
            String desc = userObject.get("desc");
            ((TextView) mCalloutBalloon.findViewById(R.id.desc)).setText(desc);
            String desc2 = userObject.get("desc2");
            ((TextView) mCalloutBalloon.findViewById(R.id.desc2)).setText(desc2);
            String desc3 = userObject.get("desc3");
            ((TextView) mCalloutBalloon.findViewById(R.id.desc3)).setText(desc3);
            String desc4 = userObject.get("desc4");
            ((TextView) mCalloutBalloon.findViewById(R.id.desc4)).setText(desc4);
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem){
            return null;
        }
    }
    private void createCustomMarker(MapView mapView){

        TestApiData apiData = new TestApiData();
        ArrayList<TestData> dataArr = apiData.getData();

        ArrayList<MapPOIItem> markerArr = new ArrayList<MapPOIItem>(); // 지도화면 위에 추가되는 POI Item에 해당하는 Class
        for (TestData data : dataArr) { // dataArr에서 차례대로 객체를 꺼내서 data에 넣음
            MapPOIItem marker = new MapPOIItem();
            HashMap<String, String> userObject = new HashMap<>();
            marker.setMapPoint(MapPoint.mapPointWithGeoCoord(data.getLatitude(), data.getLongitude()));
            marker.setItemName(data.getName());
            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
            marker.setCustomImageResourceId(R.drawable.custom_marker_red); // 마커 이미지.
            marker.setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
            marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
            userObject.put("desc" , "주소                " + data.getPname());
            userObject.put("desc2", "주차장 유형           " + data.getRname());
            userObject.put("desc3", "총 주자 가능대수      " + data.getAname());
            userObject.put("desc4", "현재 주차 대수        " + data.getOname());
            marker.setUserObject(userObject);
            markerArr.add(marker);

        }

        mapView.addPOIItems(markerArr.toArray(new MapPOIItem[markerArr.size()])); //지도화면에 POI Item 아이콘(마커) 리스트를 추가한다.

    }
    private MapView.POIItemEventListener poiItemEventListener = new MapView.POIItemEventListener() {
        @Override
        public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
            //sample code 없음
            Log.i("111111111111111","진입");
        }

        @Override
        public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
            Toast.makeText(MainActivity.this, "Clicked" + mapPOIItem.getItemName() + " Callout Balloon", Toast.LENGTH_LONG).show();
            Log.i("2222222222222222","진입");
        }

        @Override
        public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
            //sample code 없음
            Log.i("333333333333","진입");
        }

        @Override
        public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
            //sample code 없음
            Log.i("444444444444444","진입");
        }
    };

    // 권한 체크 이후로직
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        // READ_PHONE_STATE의 권한 체크 결과를 불러온다
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);
        if (requestCode == 1000) {
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            // 권한 체크에 동의를 하지 않으면 안드로이드 종료
            if (check_result == false) {
                finish();
            }
        }
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {

    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

}
// TestData class 생성
class TestData {
    String name;

    String pname;

    String Rname;

    String Oname;

    String Aname;
    Double latitude;
    Double longitude;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getPname() {
        return pname;
    }

    public void setRname(String Rname) {
        this.Rname = Rname;
    }

    public String getRname() {
        return Rname;
    }

    public void setAname(String Aname) {
        this.Aname = Aname;
    }

    public String getAname() {
        return Aname;
    }

    public void setOname(String Oname) {
        this.Oname = Oname;
    }

    public String getOname() {
        return Oname;
    }


    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "TestData{" +
                "name='" + name + '\'' +
                ", pname='" + pname + '\'' +
                ", Rname='" + Rname + '\'' +
                ", Aname='" + Aname + '\'' +
                ", Oname='" + Oname + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}


class TestApiData {
    String apiUrl = "http://openapi.seoul.go.kr:8088/";
    String apiKey = "786f4e517663686137376e6b70456e";

    public ArrayList<TestData> getData() {
        //return과 관련된 부분
        ArrayList<TestData> dataArr = new ArrayList<TestData>();

        //네트워킹 작업은 메인스레드에서 처리하면 안된다. 따로 스레드를 만들어 처리하자
        Thread t = new Thread() {
            @Override
            public void run() {
                try {

                    String ktype[] = new String[25]; // 25개의 문자열 데이터를 담을 수 있는 배열 ktype 생성
                    // 길이가 25인 string 배열 생성

                    //url과 관련된 부분
                    ktype[0] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/종로구";
                    ktype[1] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/중구";
                    ktype[2] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/용산구";
                    ktype[3] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/성동구";
                    ktype[4] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/광진구";
                    ktype[5] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/동대문구";
                    ktype[6] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/중랑구";
                    ktype[7] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/성북구";
                    ktype[8] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/강북구";
                    ktype[9] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/도봉구";
                    ktype[10] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/노원구";
                    ktype[11] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/은평구";
                    ktype[12] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/서대문구";
                    ktype[13] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/마포구";
                    ktype[14] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/양천구";
                    ktype[15] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/강서구";
                    ktype[16] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/구로구";
                    ktype[17] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/금천구";
                    ktype[18] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/영등포구";
                    ktype[19] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/동작구";
                    ktype[20] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/관악구";
                    ktype[21] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/서초구";
                    ktype[22] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/강남구";
                    ktype[23] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/송파구";
                    ktype[24] = apiUrl + apiKey + "/xml" + "/GetParkingInfo/1/500/강동구";


                    String save = "";

                    for (int i = 0; i < ktype.length; i++) {
                        String[] array = ktype[i].split("/");     //콤마 구분자로 배열에 ktype저장

                        for (String cha : array) {      //배열 갯수만큼 포문이 돌아간다.
                            // 배열 array 의 각 인덱스 데이터를 차례대로 문자열 변수 cha 에 저장
                            // 반복문 내에서 변수 cha 을 사용하면 각 배열 인덱스 데이터 사용 가능해짐
                            if (cha.equals("xml")) {
                                save = ktype[i];

                            }

                        }


                        URL url = new URL(save);
                        InputStream is = url.openStream();/*연결된 url로부터 데이터 읽어들이기
                                                            url 정보를 html로 바이트단위로 보내기 때문에 InputStream으로 받는다.
                                                            */


                        //xmlParser 생성
                        XmlPullParserFactory xmlFactory = XmlPullParserFactory.newInstance();
                        XmlPullParser parser = xmlFactory.newPullParser();
                        parser.setInput(is,"utf-8");

                        //xml과 관련된 변수들
                        boolean bName = false, bLat = false, bLong = false, pName= false, RName= false, AName= false, OName= false ;
                        String name = "", latitude = "", longitude = "", pname= "", Rname= "", Aname= "", Oname= "";

                        //본격적으로 파싱
                        while(parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                            int type = parser.getEventType();
                            TestData data = new TestData();

                            //태그 확인
                            if(type == XmlPullParser.START_TAG) { // 시작 태그부터 xml 모든 데이터 파싱
                                if (parser.getName().equals("PARKING_NAME")) {// 위치명 태그
                                    bName = true; // true 값 변수 지정
                                } else if (parser.getName().equals("LAT")) {// 위도 태그
                                    bLat = true;
                                }else if (parser.getName().equals("LNG")) {// 경도 태그
                                    bLong = true;
                                }else if (parser.getName().equals("ADDR")) {
                                    pName = true;
                                }else if (parser.getName().equals("PARKING_TYPE_NM")) {
                                    RName = true;
                                }else if (parser.getName().equals("CAPACITY")) {
                                    AName = true;
                                }else if (parser.getName().equals("CUR_PARKING")) {
                                    OName = true;
                                }
                            }
                            //내용(텍스트) 확인
                            else if(type == XmlPullParser.TEXT) {
                                if (bName) {
                                    name = parser.getText();
                                    bName = false;
                                } else if (bLat) {
                                    latitude = parser.getText();
                                    bLat = false;
                                } else if (bLong) {
                                    longitude = parser.getText();
                                    bLong = false;
                                }else if (pName) {
                                    pname = parser.getText();
                                    pName = false;
                                }else if (RName) {
                                    Rname = parser.getText();
                                    RName = false;
                                }else if (AName) {
                                    Aname = parser.getText();
                                    AName = false;
                                }else if (OName) {
                                    Oname = parser.getText();
                                    OName = false;
                                }
                            }
                            //내용 다 읽었으면 데이터 추가
                            else if (type == XmlPullParser.END_TAG && parser.getName().equals("row")) { // 엔드 태그의 이름 일치 확인
                                data.setName(name); // 데이터 네임 확인
                                data.setLatitude(Double.valueOf(latitude));
                                data.setLongitude(Double.valueOf(longitude));
                                data.setPname(pname);
                                data.setRname(Rname);
                                data.setAname(Aname);
                                data.setOname(Oname);
                                if (Rname.equals("노외 주차장")) {
                                    dataArr.add(data);
                                } else {
                                    // 공백으로 pass
                                }
                            }

                            type = parser.next();
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        };
        try {
            t.start();
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return dataArr;
    }
}