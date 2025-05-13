<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\MidtransController;

Route::get('/user', function (Request $request) {
    return $request->user();
})->middleware('auth:sanctum');

Route::post('/charge', [MidtransController::class, 'charge']);
Route::get('/checkstatus/{orderId}', [MidtransController::class, 'checkStatus']);

