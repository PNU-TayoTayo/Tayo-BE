/*
SPDX-License-Identifier: Apache-2.0
*/

package main

import (
	"crypto/sha256"
	"encoding/binary"
	"encoding/json"
	"fmt"
	"math"
	"strconv"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

type SmartContract struct {
	contractapi.Contract
}

// 차량 공유
type Sharing struct {
	ID              float64 `json:"sharingID"`
	CarID           float64 `json:"carID"`
	LenderID        float64 `json:"lenderID"`
	BorrowerID      float64 `json:"borrowerID"`
	SharingPrice    int     `json:"sharingPrice"`
	SharingTime     string  `json:"sharingTime"`
	SharingLocation string  `json:"sharingLocation"`
	SharingStatus   string  `json:"sharingStatus"`
}

// 사용자 지갑
type Wallet struct {
	ID     float64 `json:"walletID"`
	UserID float64 `json:"userID"`
	Money  int     `json:"money"`
}

type QueryResult struct {
	Key    string `json:"Key"`
	Record *Sharing
}

// ledger 초기화
func (s *SmartContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	sharings := []Sharing{
		{ID: 1, CarID: 1, LenderID: 1, BorrowerID: 2,
			SharingPrice: 20000, SharingTime: "2023-07-19 12:00", SharingLocation: "부산대학교", SharingStatus: "신청"},
	}

	for _, sharing := range sharings {
		sharingJSON, err := json.Marshal(sharing)
		if err != nil {
			return fmt.Errorf("failed to marshal sharing data: %v", err)
		}

		err = ctx.GetStub().PutState(fmt.Sprintf("%.0f", sharing.ID), sharingJSON)
		if err != nil {
			return fmt.Errorf("failed to put sharing data on ledger: %v", err)
		}
	}

	// wallets := []Wallet{
	// 	{ID: 2, UserID: 1, Money: 1000},
	// 	{ID: 3, UserID: 2, Money: 1500},
	// 	{ID: 4, UserID: 3, Money: 2000},
	// }

	// for _, wallet := range wallets {
	// 	walletJSON, err := json.Marshal(wallet)
	// 	if err != nil {
	// 		return fmt.Errorf("failed to marshal wallet data: %v", err)
	// 	}

	// 	err = ctx.GetStub().PutState(fmt.Sprintf("%.0f", wallet.ID), walletJSON)
	// 	if err != nil {
	// 		return fmt.Errorf("failed to put wallet data on ledger: %v", err)
	// 	}
	// }

	return nil
}

// 사용자가 대여 신청 누르면 발생 -> Sharing 객체를 만들어줌 - 연결 완료
func (s *SmartContract) CreateSharing(ctx contractapi.TransactionContextInterface, sharing Sharing) error {
	sharingAsBytes, err := json.Marshal(sharing)
	if err != nil {
		return fmt.Errorf("failed to marshal sharing data: %v", err)
	}
	return ctx.GetStub().PutState(fmt.Sprintf("%.0f", sharing.ID), sharingAsBytes)
}

// TODO - 수정 필요
// sharing 구조체 업데이트 함수
func (s *SmartContract) UpdateSharing(ctx contractapi.TransactionContextInterface, carID string, sharingStatus string, sharingPrice string, sharingTime string, sharingLocation string) error {
	carIDFloat, err := strconv.ParseFloat(carID, 64)
	sharingPriceInt, err := strconv.Atoi(sharingPrice)

	// carID를 sharingID로 변환 -> string 값 반환
	sharingID := computeUniqueID(carIDFloat)

	sharingAsBytes, err := ctx.GetStub().GetState(fmt.Sprintf("%.0f", sharingID))
	if err != nil {
		return fmt.Errorf("failed to read from world state: %v", err)
	}
	if sharingAsBytes == nil {
		return fmt.Errorf("the sharing %s does not exist", sharingID)
	}

	// 기존 sharing 구조체 업데이트
	sharing := new(Sharing)
	err = json.Unmarshal(sharingAsBytes, sharing)
	if err != nil {
		return fmt.Errorf("failed to unmarshal sharing data: %v", err)
	}

	// 업데이트할 필드만 값 변경
	if sharingStatus != "" {
		sharing.SharingStatus = sharingStatus
	}
	if sharingPriceInt >= 0 {
		sharing.SharingPrice = sharingPriceInt
	}
	if sharingTime != "" {
		sharing.SharingTime = sharingTime
	}
	if sharingLocation != "" {
		sharing.SharingLocation = sharingLocation
	}

	sharingAsBytes, err = json.Marshal(sharing)
	if err != nil {
		return fmt.Errorf("failed to marshal sharing data: %v", err)
	}

	// 업데이트된 sharing 정보를 월드 스테이트에 저장
	err = ctx.GetStub().PutState(fmt.Sprintf("%.0f", sharingID), sharingAsBytes)
	if err != nil {
		return fmt.Errorf("failed to put sharing data on ledger: %v", err)
	}

	return nil
}

// 재화 거래 과정
func (s *SmartContract) ProcessTransaction(ctx contractapi.TransactionContextInterface, carID string, lenderID string, borrowerID string, sharingPrice string) error {
	carIDFloat, err := strconv.ParseFloat(carID, 64)
	lenderIDFloat, err := strconv.ParseFloat(lenderID, 64)
	borrowerIDFloat, err := strconv.ParseFloat(borrowerID, 64)
	sharingPriceInt, err := strconv.Atoi(sharingPrice)

	// carID를 sharingID로 변환
	sharingID := computeUniqueID(carIDFloat)
	sharing, err := s.ReadSharingByID(ctx, sharingID)
	if err != nil {
		return err
	}

	if sharing.SharingStatus == "확정" {
		// 임대인, 임차인 id로 지갑 불러오기
		lenderWallet, err := s.ReadWalletByUserID(ctx, lenderIDFloat)
		if err != nil {
			return err
		}

		borrowerWallet, err := s.ReadWalletByUserID(ctx, borrowerIDFloat)
		if err != nil {
			return err
		}

		// 재화 설정
		lenderWallet.Money -= sharingPriceInt
		borrowerWallet.Money += sharingPriceInt

		// 지갑 업데이트
		err = s.UpdateUserWallet(ctx, lenderWallet)
		if err != nil {
			return err
		}

		err = s.UpdateUserWallet(ctx, borrowerWallet)
		if err != nil {
			return err
		}
	}

	return nil
}

// ----------------------------------------------------------------------------------------
// ----------------------------------------------------------------------------------------
// 										Wallet 관련 체인코드
// ----------------------------------------------------------------------------------------
// ----------------------------------------------------------------------------------------

// 회원가입 시 사용자 지갑 생성 - 연결 완료
func (s *SmartContract) CreateWallet(ctx contractapi.TransactionContextInterface, userID string) error {
	userIDFloat, err := strconv.ParseFloat(userID, 64)

	// userID로 만드는 walletID
	walletID := computeUniqueID(userIDFloat)

	exists, err := s.WalletExists(ctx, walletID)
	if err != nil {
		return err
	}
	if exists {
		return fmt.Errorf("the wallet %s already exists", walletID)
	}

	money := 100000
	wallet := Wallet{
		ID:     walletID,
		UserID: userIDFloat,
		Money:  money,
	}

	walletJSON, err := json.Marshal(wallet)
	if err != nil {
		return err
	}
	err = ctx.GetStub().PutState(fmt.Sprintf("%.0f", walletID), walletJSON)
	if err != nil {
		return err
	}

	return nil
}

// 사용자 지갑 업데이트
func (s *SmartContract) UpdateUserWallet(ctx contractapi.TransactionContextInterface, wallet *Wallet) error {
	wallet, err := s.ReadWallet(ctx, wallet.ID)
	if err != nil {
		return err
	}

	walletJSON, err := json.Marshal(wallet)
	if err != nil {
		return err
	}

	err = ctx.GetStub().PutState(fmt.Sprintf("%.0f", wallet.ID), walletJSON)
	if err != nil {
		return err
	}

	return nil
}

// 잔액 조회를 위한 체인코드
func (s *SmartContract) QueryWalletBalance(ctx contractapi.TransactionContextInterface, userID string) (int, error) {
	userIDFloat, err := strconv.ParseFloat(userID, 64)

	// userID로 만드는 walletID
	walletID := computeUniqueID(userIDFloat)

	// 지갑 정보 조회
	walletAsBytes, err := ctx.GetStub().GetState(fmt.Sprintf("%.0f", walletID))
	if err != nil {
		return 0, fmt.Errorf("지갑 정보를 조회하는 중 오류 발생: %v", err)
	}

	// 지갑 정보가 없으면 오류 반환
	if walletAsBytes == nil {
		return 0, fmt.Errorf("해당 사용자의 지갑이 존재하지 않습니다")
	}

	// JSON 디코딩을 통해 지갑 구조체로 변환
	var wallet Wallet
	err = json.Unmarshal(walletAsBytes, &wallet)
	if err != nil {
		return 0, fmt.Errorf("지갑 정보를 디코딩하는 중 오류 발생: %v", err)
	}

	// 잔액 반환
	return wallet.Money, nil
}

// 입출금 공통 함수
func (s *SmartContract) performTransaction(ctx contractapi.TransactionContextInterface, userID string, amount int, isWithdraw bool) error {
	userIDFloat, err := strconv.ParseFloat(userID, 64)

	// userID로 만드는 walletID
	walletID := computeUniqueID(userIDFloat)

	// 지갑 정보 조회
	walletAsBytes, err := ctx.GetStub().GetState(fmt.Sprintf("%.0f", walletID))
	if err != nil {
		return fmt.Errorf("지갑 정보를 조회하는 중 오류 발생: %v", err)
	}

	// 지갑 정보가 없으면 오류 반환
	if walletAsBytes == nil {
		return fmt.Errorf("해당 사용자의 지갑이 존재하지 않습니다")
	}

	// JSON 디코딩을 통해 지갑 구조체로 변환
	var wallet Wallet
	err = json.Unmarshal(walletAsBytes, &wallet)
	if err != nil {
		return fmt.Errorf("지갑 정보를 디코딩하는 중 오류 발생: %v", err)
	}

	// 출금인 경우 잔액 확인
	if isWithdraw && wallet.Money < amount {
		return fmt.Errorf("잔액이 충분하지 않습니다")
	}

	// 입금 또는 출금
	if isWithdraw {
		wallet.Money -= amount
	} else {
		wallet.Money += amount
	}

	// 지갑 정보 업데이트
	updatedWalletAsBytes, err := json.Marshal(wallet)
	if err != nil {
		return fmt.Errorf("지갑 정보를 업데이트하는 중 오류 발생: %v", err)
	}
	err = ctx.GetStub().PutState(fmt.Sprintf("%.0f", walletID), updatedWalletAsBytes)
	if err != nil {
		return fmt.Errorf("지갑 정보를 저장하는 중 오류 발생: %v", err)
	}

	return nil
}

// 출금을 위한 체인코드
func (s *SmartContract) Withdraw(ctx contractapi.TransactionContextInterface, userID string, amount string) error {
	amountInt, err := strconv.Atoi(amount)
	if err != nil {
		return fmt.Errorf("출금 금액을 정수로 변환하는 중 오류 발생: %v", err)
	}
	return s.performTransaction(ctx, userID, amountInt, true)
}

// 입금을 위한 체인코드
func (s *SmartContract) Deposit(ctx contractapi.TransactionContextInterface, userID string, amount string) error {
	amountInt, err := strconv.Atoi(amount)
	if err != nil {
		return fmt.Errorf("입금 금액을 정수로 변환하는 중 오류 발생: %v", err)
	}
	return s.performTransaction(ctx, userID, amountInt, false)
}

// ----------------------------------------------------------------------------------------
// ----------------------------------------------------------------------------------------
// 										id로 조회 체인코드
// ----------------------------------------------------------------------------------------
// ----------------------------------------------------------------------------------------

// sharingID로 조회 -> sharing
func (s *SmartContract) ReadSharingByID(ctx contractapi.TransactionContextInterface, sharingID float64) (*Sharing, error) {
	sharingAsBytes, err := ctx.GetStub().GetState(fmt.Sprintf("%.0f", sharingID))
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}

	if sharingAsBytes == nil {
		return nil, fmt.Errorf("sharing with ID %.0f does not exist", sharingID)
	}

	sharing := new(Sharing)
	err = json.Unmarshal(sharingAsBytes, sharing)
	if err != nil {
		return nil, fmt.Errorf("failed to unmarshal sharing data: %v", err)
	}

	return sharing, nil
}

// walletID로 이미 존재하는 지갑인지 판단 -> 없으면 지갑 생성하는 데 사용
func (s *SmartContract) WalletExists(ctx contractapi.TransactionContextInterface, walletID float64) (bool, error) {
	walletJSON, err := ctx.GetStub().GetState(fmt.Sprintf("%.0f", walletID))
	if err != nil {
		return false, fmt.Errorf("failed to read from world state: %v", err)
	}

	return walletJSON != nil, nil
}

// walletID로 조회 -> wallet
func (s *SmartContract) ReadWallet(ctx contractapi.TransactionContextInterface, walletID float64) (*Wallet, error) {
	walletJSON, err := ctx.GetStub().GetState(fmt.Sprintf("%.0f", walletID))
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}
	if walletJSON == nil {
		return nil, fmt.Errorf("the wallet %s does not exist", walletID)
	}

	var wallet Wallet
	err = json.Unmarshal(walletJSON, &wallet)
	if err != nil {
		return nil, err
	}

	return &wallet, nil
}

// userID로 조회 -> wallet
func (s *SmartContract) ReadWalletByUserID(ctx contractapi.TransactionContextInterface, userID float64) (*Wallet, error) {
	walletID := computeUniqueID(userID)

	return s.ReadWallet(ctx, walletID)
}

// sha256으로 Unique한 ID 계산
// sharingID는 carID로 만들고, userID는 walletID로 만듦
func computeUniqueID(id float64) float64 {
	// float64 타입을 바이트 슬라이스로 변환
	idBytes := make([]byte, 8)
	binary.LittleEndian.PutUint64(idBytes, math.Float64bits(id))

	// sha256으로 Unique한 ID 계산
	hash := sha256.New()
	hash.Write(idBytes)
	md := hash.Sum(nil)

	// 바이트 슬라이스를 다시 float64로 변환
	result := math.Float64frombits(binary.LittleEndian.Uint64(md))

	return result
}

func main() {

	chaincode, err := contractapi.NewChaincode(new(SmartContract))

	if err != nil {
		fmt.Printf("Error create tayosharing chaincode: %s", err.Error())
		return
	}

	if err := chaincode.Start(); err != nil {
		fmt.Printf("Error starting tayosharing chaincode: %s", err.Error())
	}
}
