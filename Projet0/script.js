function initMap(){
    const coordinates = [
        {lat: 48.8566, lng: 2.3522}, //Paris
        {lat: 47.3215806, lng: 5.0414701}, //Dijon
        {lat: 47.3900474, lng: 0.6889268}//Tours
    ];

    const map = new google.maps.Map(document.getElementById('map'), {
        center: coordinates[0],
        zoom: 6
    });

    const flightPath = new google.maps.Polyline({
        path: coordinates,
        geodesic: true,
        strokeColor: '#FF0000',
        strokeOpacity: 1.0,
        strokeWeight: 2
    });

    flightPath.setMap(map);
}