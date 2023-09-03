/*
SPDX-License-Identifier: Apache-2.0
*/

package main

import (
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

type SmartContract struct {
	contractapi.Contract
}

// 차량 공유
type Sharing struct {
	ID              string  `json:"id"`
	CarID           string  `json:"carID"`
	LenderID        float64 `json:"lenderID"`
	BorrowerID      float64 `json:"borrowerID"`
	SharingPrice    int     `json:"sharingPrice"`
	SharingTime     string  `json:"sharingTime"`
	SharingLocation string  `json:"sharingLocation"`
	SharingStatus   string  `json:"sharingStatus"`
}

// 사용자 지갑
type Wallet struct {
	ID     string  `json:"id"`
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
		{ID: "testSharing", CarID: "testCar", LenderID: 1, BorrowerID: 2,
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

	wallets := []Wallet{
		{ID: "testWallet1", UserID: 1, Money: 1000},
		{ID: "testWallet2", UserID: 2, Money: 1500},
		{ID: "testWallet3", UserID: 3, Money: 2000},
	}

	for _, wallet := range wallets {
		walletJSON, err := json.Marshal(wallet)
		if err != nil {
			return fmt.Errorf("failed to marshal wallet data: %v", err)
		}

		err = ctx.GetStub().PutState(fmt.Sprintf("%.0f", wallet.ID), walletJSON)
		if err != nil {
			return fmt.Errorf("failed to put wallet data on ledger: %v", err)
		}
	}

	return nil
}

// 사용자가 대여 신청 누르면 발생 -> Sharing 객체를 만들어줌
func (s *SmartContract) CreateSharing(ctx contractapi.TransactionContextInterface, sharing Sharing) error {
	sharingAsBytes, err := json.Marshal(sharing)
	if err != nil {
		return fmt.Errorf("failed to marshal sharing data: %v", err)
	}

	return ctx.GetStub().PutState(sharing.ID, sharingAsBytes)
}

// sharing 구조체 업데이트 함수
func (s *SmartContract) UpdateSharing(ctx contractapi.TransactionContextInterface, carID string, sharingStatus string, sharingPrice int, sharingTime string, sharingLocation string) error {
	// carID를 sharingID로 변환
	id := computeUniqueID([]byte(carID))

	sharingAsBytes, err := ctx.GetStub().GetState(id)
	if err != nil {
		return fmt.Errorf("failed to read from world state: %v", err)
	}
	if sharingAsBytes == nil {
		return fmt.Errorf("the sharing %s does not exist", id)
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
	if sharingPrice >= 0 {
		sharing.SharingPrice = sharingPrice
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
	err = ctx.GetStub().PutState(id, sharingAsBytes)
	if err != nil {
		return fmt.Errorf("failed to put sharing data on ledger: %v", err)
	}

	return nil
}

// 재화 거래 과정
func (s *SmartContract) ProcessTransaction(ctx contractapi.TransactionContextInterface, carID string, lenderID float64, borrowerID float64, sharingPrice int) error {
	// carID를 sharingID로 변환
	sharingID := computeUniqueID([]byte(carID))
	sharing, err := s.ReadSharingByID(ctx, sharingID)
	if err != nil {
		return err
	}

	if sharing.SharingStatus == "확정" {
		// 임대인, 임차인 id로 지갑 불러오기
		lenderWallet, err := s.ReadWalletByUserID(ctx, lenderID)
		if err != nil {
			return err
		}

		borrowerWallet, err := s.ReadWalletByUserID(ctx, borrowerID)
		if err != nil {
			return err
		}

		// 재화 설정
		lenderWallet.Money -= sharingPrice
		borrowerWallet.Money += sharingPrice

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

// 회원가입 시 사용자 지갑 생성
func (s *SmartContract) CreateWallet(ctx contractapi.TransactionContextInterface, userID float64) (string, error) {
	id := computeUniqueID([]byte(fmt.Sprintf("%.0f", userID)))

	exists, err := s.WalletExists(ctx, id)
	if err != nil {
		return "", err
	}
	if exists {
		return "", fmt.Errorf("the wallet %s already exists", id)
	}

	money := 100000
	wallet := Wallet{
		ID:     id,
		UserID: userID,
		Money:  money,
	}

	walletJSON, err := json.Marshal(wallet)
	if err != nil {
		return "", err
	}
	err = ctx.GetStub().PutState(id, walletJSON)
	if err != nil {
		return "", err
	}

	return id, nil
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

	err = ctx.GetStub().PutState(wallet.ID, walletJSON)
	if err != nil {
		return err
	}

	return nil
}

// ----------------------------------------------------------------------------------------
// ----------------------------------------------------------------------------------------
// 										id로 조회 체인코드
// ----------------------------------------------------------------------------------------
// ----------------------------------------------------------------------------------------

// sharing id로 조회 -> sharing
func (s *SmartContract) ReadSharingByID(ctx contractapi.TransactionContextInterface, sharingID string) (*Sharing, error) {
	sharingAsBytes, err := ctx.GetStub().GetState(sharingID)
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

// 지갑 id로 이미 존재하는 지갑인지 판단 -> 없으면 지갑 생성하는 데 사용
func (s *SmartContract) WalletExists(ctx contractapi.TransactionContextInterface, id string) (bool, error) {
	walletJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return false, fmt.Errorf("failed to read from world state: %v", err)
	}

	return walletJSON != nil, nil
}

// 지갑 id로 조회 -> wallet
func (s *SmartContract) ReadWallet(ctx contractapi.TransactionContextInterface, id string) (*Wallet, error) {
	walletJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}
	if walletJSON == nil {
		return nil, fmt.Errorf("the wallet %s does not exist", id)
	}

	var wallet Wallet
	err = json.Unmarshal(walletJSON, &wallet)
	if err != nil {
		return nil, err
	}

	return &wallet, nil
}

// 사용자 id로 조회 -> wallet
func (s *SmartContract) ReadWalletByUserID(ctx contractapi.TransactionContextInterface, userID float64) (*Wallet, error) {
	walletID := computeUniqueID([]byte(fmt.Sprintf("%.0f", userID)))

	return s.ReadWallet(ctx, walletID)
}

// sha256으로 Unique한 ID 계산
// sharingID는 carID로 만들고, walletID는 userID로 만듦
func computeUniqueID(id []byte) string {
	hash := sha256.New()
	hash.Write(id)
	md := hash.Sum(nil)
	return hex.EncodeToString(md)
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
