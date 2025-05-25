<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\MidtransController;
use App\Http\Controllers\GeoController;

Route::get('/user', function (Request $request) {
    return $request->user();
})->middleware('auth:sanctum');

Route::post('/charge', [MidtransController::class, 'charge']);
Route::get('/checkstatus/{orderId}', [MidtransController::class, 'checkStatus']);

Route::get('/geocode', [GeoController::class, 'geocode']);        
Route::get('/reverse', [GeoController::class, 'reverseGeocode']); 