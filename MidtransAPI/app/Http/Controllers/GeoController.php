<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;

class GeoController extends Controller
{
    public function geocode(Request $request)
    {
        $address = $request->query('address');

        $response = Http::get('https://api.opencagedata.com/geocode/v1/json', [
            'q' => $address,
            'key' => config('services.opencage.key'),
            'limit' => 1,
        ]);

        return response()->json($response->json());
    }

    public function reverseGeocode(Request $request)
    {
        $base_url = "https://nominatim.openstreetmap.org";
        $lat = $request->query('lat');
        $lon = $request->query('lon');

        $response = Http::withHeaders([
                'User-Agent' => 'LelanginAja/1.0 rapha.hanley@gmail.com', 
            ])->get($base_url . '/reverse', [
                'lat' => $lat,
                'lon' => $lon,
                'format' => 'json',
            ]);

        return response()->json($response->json());
    }
}
