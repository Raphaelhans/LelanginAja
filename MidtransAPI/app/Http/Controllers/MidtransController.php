<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;

class MidtransController extends Controller
{
    public function charge(Request $request)
    {
        $serverKey = config('services.midtrans.server_key');
        $apiUrl = 'https://app.sandbox.midtrans.com/snap/v1/transactions';

        $response = Http::withHeaders([
            'Authorization' => 'Basic ' . base64_encode($serverKey . ':'),
            'Content-Type' => 'application/json',
            'Accept' => 'application/json',
        ])->post($apiUrl, $request->all());

        return response()->json($response->json(), $response->status());
    }

    public function checkStatus($orderId)
    {
        $serverKey = config('services.midtrans.server_key');
        $encodedKey = base64_encode($serverKey . ':');

        $response = Http::withHeaders([
            'Authorization' => 'Basic ' . $encodedKey,
            'Accept' => 'application/json'
        ])->get("https://api.sandbox.midtrans.com/v2/{$orderId}/status");

        return response()->json($response->json(), $response->status());
    }
}
