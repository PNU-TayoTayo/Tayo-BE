/*
SPDX-License-Identifier: Apache-2.0
*/

package main

import (
	"encoding/json"
	"fmt"
	"strconv"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

type SmartContract struct {
	contractapi.Contract
}

// 변경 가능한 정보에 * 표시 (Indy 지갑에서 불러오는 값이 아니라, 사용자가 직접 입력하는 값)
// 업데이트 (변경) 시 해당 필드 값만 변경하도록 함
type Car struct {
	ID                     float64  `json:"carID"`
	OwnerID                float64  `json:"ownerID"`
	Model                  string   `json:"model"`
	Engine                 string   `json:"engine"`
	DeliveryDate           string   `json:"deliveryDate"`
	DrivingRecord          int      `json:"drivingRecord"`
	InspectionRecord       string   `json:"inspectionRecord"`
	DateList               []string `json:"dateList"`               // *
	SharingLocation        string   `json:"sharingLocation"`        // *
	SharingLocationAddress string   `json:"sharingLocationAddress"` // *
	SharingLatitude        float64  `json:"sharingLatitude"`        // *
	SharingLongitude       float64  `json:"sharingLongitude"`       // *
	SharingAvailable       bool     `json:"sharingAvailable"`       // *
	SharingRating          int      `json:"sharingRating"`          // *
}

type QueryResult struct {
	Key    string `json:"Key"`
	Record *Car
}

// ledger 초기화
func (s *SmartContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	cars := []Car{
		{ID: 1, OwnerID: 1, Model: "Model1", Engine: "Engine1",
			DeliveryDate: "2023-07-19", DrivingRecord: 250, InspectionRecord: "2021-11-20",
			DateList:        []string{"2023-07-19", "2023-07-20"},
			SharingLocation: "부산대학교 부산캠퍼스농구장", SharingLocationAddress: "부산 금정구 부산대학로63번길 2",
			SharingLatitude: 12.500, SharingLongitude: 67.89, SharingAvailable: true, SharingRating: 0},

		{ID: 2, OwnerID: 1, Model: "Model2", Engine: "Engine2",
			DeliveryDate: "2023-07-19", DrivingRecord: 250, InspectionRecord: "2022-04-06",
			DateList:        []string{"2023-07-19", "2023-07-20"},
			SharingLocation: "부산대학교 부산캠퍼스농구장", SharingLocationAddress: "부산 금정구 부산대학로63번길 2",
			SharingLatitude: 46.000, SharingLongitude: 88.000, SharingAvailable: true, SharingRating: 0},

		{ID: 3, OwnerID: 3, Model: "Model2", Engine: "Engine2",
			DeliveryDate: "2023-07-19", DrivingRecord: 250, InspectionRecord: "2022-04-06",
			DateList:        []string{"2023-07-19", "2023-07-20"},
			SharingLocation: "부산대학교 부산캠퍼스농구장", SharingLocationAddress: "부산 금정구 부산대학로63번길 2",
			SharingLatitude: 40.7128, SharingLongitude: -74.0060, SharingAvailable: true, SharingRating: 0},
	}

	for _, car := range cars {
		carJSON, err := json.Marshal(car)
		if err != nil {
			return fmt.Errorf("failed to marshal car data: %v", err)
		}

		err = ctx.GetStub().PutState(fmt.Sprintf("%.0f", car.ID), carJSON)
		if err != nil {
			return fmt.Errorf("failed to put car data on ledger: %v", err)
		}
	}

	return nil
}

// 차량 등록
func (s *SmartContract) CreateCar(ctx contractapi.TransactionContextInterface, car Car) error {
	carAsBytes, err := json.Marshal(car)
	if err != nil {
		return fmt.Errorf("failed to marshal car data: %v", err)
	}

	return ctx.GetStub().PutState(fmt.Sprintf("%.0f", car.ID), carAsBytes)
}

// 차량 삭제
func (s *SmartContract) DeleteCar(ctx contractapi.TransactionContextInterface, carID string) error {
	carIDFloat, err := strconv.ParseFloat(carID, 64)
	if err != nil {
		return err
	}

	_, err = getCarByID(ctx, carIDFloat)
	if err != nil {
		return err
	}

	err = ctx.GetStub().DelState(fmt.Sprintf("%.0f", carID))
	if err != nil {
		return fmt.Errorf("failed to delete car data from ledger: %v", err)
	}

	return nil
}

// carID(key값, unique)로 차량 개별 조회 - 연결 완료
func (s *SmartContract) QueryCarByCarID(ctx contractapi.TransactionContextInterface, carID float64) (*Car, error) {
	carAsBytes, err := getCarByID(ctx, carID)
	if err != nil {
		return nil, err
	}

	car := new(Car)
	err = json.Unmarshal(carAsBytes, car)
	if err != nil {
		return nil, fmt.Errorf("failed to unmarshal car data: %v", err)
	}

	return car, nil
}

// ownerID로 차량 조회 - 연결 완료
func (s *SmartContract) QueryCarByOwnerID(ctx contractapi.TransactionContextInterface, ownerID string) ([]*Car, error) {
	ownerIDFloat, err := strconv.ParseFloat(ownerID, 64)

	queryString := fmt.Sprintf(`{"selector":{"ownerID":%.0f}}`, ownerIDFloat)
	resultsIterator, err := ctx.GetStub().GetQueryResult(queryString)
	if err != nil {
		return nil, fmt.Errorf("failed to get query result: %v", err)
	}
	defer resultsIterator.Close()

	var cars []*Car
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, fmt.Errorf("failed to get next query result: %v", err)
		}

		var car Car
		err = json.Unmarshal(queryResponse.Value, &car)
		if err != nil {
			return nil, fmt.Errorf("failed to unmarshal car data: %v", err)
		}
		cars = append(cars, &car)
	}

	return cars, nil
}

// carID로 차량의 available 상태 체크 => 사용 안 될 수도 있는데 일단 남겨둠
func (s *SmartContract) IsCarAvailable(ctx contractapi.TransactionContextInterface, carID string) (bool, error) {
	carIDFloat, err := strconv.ParseFloat(carID, 64)

	carAsBytes, err := getCarByID(ctx, carIDFloat)
	if err != nil {
		return false, err
	}

	car := new(Car)
	err = json.Unmarshal(carAsBytes, car)
	if err != nil {
		return false, fmt.Errorf("failed to unmarshal car data: %v", err)
	}

	return car.SharingAvailable, nil
}

// 위,경도 및 대여 날짜 기반 available인 차량 검색
func (s *SmartContract) GetAvailableCars(ctx contractapi.TransactionContextInterface,
	leftLatitudeStr string, leftLongitudeStr string, rightLatitudeStr string, rightLongitudeStr string, date string) ([]*Car, error) {
	leftLatitude, err := strconv.ParseFloat(leftLatitudeStr, 64)
	leftLongitude, err := strconv.ParseFloat(leftLongitudeStr, 64)
	rightLatitude, err := strconv.ParseFloat(rightLatitudeStr, 64)
	rightLongitude, err := strconv.ParseFloat(rightLongitudeStr, 64)

	resultsIterator, err := ctx.GetStub().GetStateByRange("", "")
	if err != nil {
		return nil, fmt.Errorf("Failed to get state by range: %v", err)
	}
	defer resultsIterator.Close()

	// 결과를 저장할 슬라이스 초기화
	availableCars := []*Car{}

	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, fmt.Errorf("Error reading query response: %v", err)
		}

		car := new(Car)
		err = json.Unmarshal(queryResponse.Value, car)
		if err != nil {
			return nil, fmt.Errorf("Error unmarshaling car data: %v", err)
		}

		// 차량의 위치가 위치 범위 내에 있고, date가 DateList에 포함되어 있으며 SharingAvailable이 true일 때 결과에 추가
		if car.SharingLatitude >= leftLatitude && car.SharingLatitude <= rightLatitude &&
			car.SharingLongitude >= leftLongitude && car.SharingLongitude <= rightLongitude &&
			dateInDateList(date, car.DateList) &&
			car.SharingAvailable {
			availableCars = append(availableCars, car)
		}
	}

	return availableCars, nil
}

// 주어진 date 문자열이 DateList 슬라이스에 포함되어 있는지 확인
func dateInDateList(date string, dateList []string) bool {
	for _, d := range dateList {
		if d == date {
			return true
		}
	}
	return false
}

// carID로 차량 정보 업데이트 함수
func (s *SmartContract) UpdateCar(ctx contractapi.TransactionContextInterface, carID float64, dateList []string, sharingLocation string, sharingLocationAddress string, sharingLatitude float64, sharingLongitude float64, sharingAvailable bool, sharingRating int) error {
	carAsBytes, err := getCarByID(ctx, carID)
	if err != nil {
		return err
	}

	car := new(Car)
	err = json.Unmarshal(carAsBytes, car)
	if err != nil {
		return fmt.Errorf("failed to unmarshal car data: %v", err)
	}

	// 업데이트할 필드만 값 변경
	if len(dateList) > 0 {
		car.DateList = dateList
	}
	if sharingLocation != "" {
		car.SharingLocation = sharingLocation
	}
	if sharingLocationAddress != "" {
		car.SharingLocationAddress = sharingLocationAddress
	}
	if sharingLatitude != 0 {
		car.SharingLatitude = sharingLatitude
	}
	if sharingLongitude != 0 {
		car.SharingLongitude = sharingLongitude
	}
	if sharingAvailable {
		car.SharingAvailable = sharingAvailable
	}
	if sharingRating >= 0 {
		car.SharingRating = sharingRating
	}

	carAsBytes, err = json.Marshal(car)
	if err != nil {
		return fmt.Errorf("failed to marshal car data: %v", err)
	}

	err = ctx.GetStub().PutState(fmt.Sprintf("%.0f", car.ID), carAsBytes)
	if err != nil {
		return fmt.Errorf("failed to put car data on ledger: %v", err)
	}

	return nil
}

// world state에 저장된 모든 차량 검색
func (s *SmartContract) QueryAllCars(ctx contractapi.TransactionContextInterface) ([]QueryResult, error) {
	startKey := ""
	endKey := ""

	resultsIterator, err := ctx.GetStub().GetStateByRange(startKey, endKey)

	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()

	results := []QueryResult{}

	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()

		if err != nil {
			return nil, err
		}

		car := new(Car)
		_ = json.Unmarshal(queryResponse.Value, car)

		queryResult := QueryResult{Key: queryResponse.Key, Record: car}
		results = append(results, queryResult)
	}

	return results, nil
}

// carID로 차량 조회하면서 에러 체크하는 내부 함수 (공통 사용됨)
func getCarByID(ctx contractapi.TransactionContextInterface, carID float64) ([]byte, error) {
	carAsBytes, err := ctx.GetStub().GetState(fmt.Sprintf("%.0f", carID))
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}

	if carAsBytes == nil {
		return nil, fmt.Errorf("car with ID %.0f does not exist", carID)
	}

	return carAsBytes, nil
}

func main() {

	chaincode, err := contractapi.NewChaincode(new(SmartContract))

	if err != nil {
		fmt.Printf("Error create tayocar chaincode: %s", err.Error())
		return
	}

	if err := chaincode.Start(); err != nil {
		fmt.Printf("Error starting tayocar chaincode: %s", err.Error())
	}
}
