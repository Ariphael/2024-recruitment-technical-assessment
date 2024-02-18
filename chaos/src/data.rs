use axum::{http::StatusCode, response::IntoResponse, Json};
use serde::{Deserialize, Serialize};

#[derive(Debug, Deserialize)]
#[serde(untagged)]
pub enum Data {
    Int(u32),
    String(String),
}

#[derive(Debug, Deserialize)]
pub struct DataRequest {
    data: Vec<Data>
}

#[derive(Serialize)]
pub struct DataResponse {
    string_len: u32,
    int_sum: u32
}

pub async fn process_data(Json(request): Json<DataRequest>) -> impl IntoResponse {
    println!("Received JSON data: {:?}", request);
    // Calculate sums and return response
    let mut string_len: u32 = 0;
    let mut int_sum: u32 = 0;

    for data in request.data.iter() {
        match data {
            Data::String(string_data) => string_len += string_data.len() as u32,
            Data::Int(int_data) => int_sum += *int_data as u32,
        }
    }

    let response = DataResponse {
        string_len,
        int_sum
    };

    (StatusCode::OK, Json(response))
}