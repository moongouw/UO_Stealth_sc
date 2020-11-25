Program Seller_mining_Minoc;
{$Include 'all.inc'}
/////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////    Настройки    //////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////

const
ThisSell = $23C8; // Type того что продаём
SellPack = $505E9178; // ID пака для продажи
VendorID = $00001A1D; // ID вендора кому продаём
BagGoldID = $4FFCD953; // ID пака с голдой

/////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////  Начало блоков  //////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////

procedure OpenPack(OpenItem : Cardinal);
begin
    if dead and not Connected then exit;
    if (LastContainer <> OpenItem) and (dead = false) then
    begin
      repeat
        if not Connected then exit;
        CheckLag(5000);
        //Checksave;
        UseObject(OpenItem);
        wait(1000);
      until LastContainer = OpenItem;
    end;
end;

function WaitS : Boolean;
var
g, t : integer;
begin
    if Connected and dead then exit;
    g := StrToInt(GetGlobal('stealth', 'NeedSell'));
    if g > 0 then
    begin
        repeat
            t := 0;
            if GetARStatus then SetARStatus(True);
            if not Connected then Connect;
            repeat
                t := t + 1;
                wait(100);
            until Connected or (t >= 150);
            wait(5000);
        until Connected;
        OpenPack(Backpack);
        result := True;
        exit;
    end
    else
    begin
        repeat
            t := 0;
            if GetARStatus then SetARStatus(False);
            if Connected then Disconnect;
            repeat
                t := t + 1;
                wait(100);
            until not Connected  or (t >= 150);
        until not Connected;
    end;
    result := False; 
end;

function CheckPlace : Integer;
begin
    if dead and not Connected then exit;
    if (GetX(Self) <= 2594) and (GetY(Self) <= 499) and (GetX(Self) >= 2557) and (GetY(Self) >= 445) then
    begin
        result := 1; // В шахте
    end
    else
    begin
        result := 2; // Вне шахты;
    end;    
end;

function CheckSellM : Boolean;
begin
    if dead and not Connected then exit;
    OpenPack(Backpack);
    OpenPack(SellPack);
    FindTypeEx(ThisSell, $FFFF, SellPack, True);
    CheckLag(5000);
    if FindCount > 0 then Result := True;   
end;

function CheckWeight : Boolean;
begin
    if Weight < MaxWeight then Result := True; 
    if Weight >= MaxWeight then Result := False;
end;

procedure MoveOut;
var
CountG, i : integer;
begin
    if dead and not Connected then exit;
    If IsGump then
    begin
        CountG := GetGumpsCount;
        for i := 0 to CountG do CloseSimpleGump(i);  
    end;
    WaitGump('1025');
    NewMoveXY(2558, 497, True, 0, CheckWeight);
    repeat
        wait(100);
    until (GetY(Self)<>497);        
end;

procedure GoToGermes;
begin
    if dead and not Connected then exit;
    if (GetX(Self) = 2593) and (GetY(Self) = 461) then exit;
    if CheckPlace = 2 then
    begin
        if (GetX(Self) = 2558) and (GetY(Self) = 503) then NewMoveXY(2560,505,True,0,CheckWeight); 
        NewMoveXY(2558,503,True,0,CheckWeight);
        repeat
            wait(100);
        until (GetX(Self) <> 2558) and (GetY(Self) <> 503);
    end;
    NewMoveXY(2593,461,True,0,CheckWeight);
end;

function CheckGold : Boolean;
begin
    if dead and not Connected then exit;
    FindTypeEx($0EED, $0000, Backpack, True);
    if (FindCount > 0) and (FindQuantity > 300) then Result := True;
end;

procedure DropGold;
var
t, h : integer;
bank : Cardinal;
begin
    if dead and not Connected then exit;
    if CheckPlace = 1 then MoveOut;
    if CheckPlace = 2 then
    begin
        if (GetX(Self) <> 2513) and (GetY(Self) <> 545) then NewMoveXY(2513,545,True,0,CheckWeight);
        UOSay('bank');
        wait(500);
        repeat
            CheckLag(5000);
            bank := ObjAtLayer(BankLayer);
            UseObject(BagGoldID);
            t := 0;
            repeat 
                wait(100);
                t := t + 1;
            until (LastContainer = BagGoldID) or (t >= 100);
            if LastContainer <> BagGoldID then UOSay('bank');
        until LastContainer = BagGoldID;
        FindTypeEx($0EED, $0000, Backpack, True);
        if FindCount > 0 then
            if FindQuantity > 300 then MoveItem(FindItem, FindQuantity - 300, BagGoldID, 0,0,0);
            wait(1000);
        FindTypeEx($0EED, $FFFF, BagGoldID, True);
        AddToSystemJournal('В банке '+IntToStr(FindFullQuantity)+' голды.');
        FindTypeEx($097B, $FFFF, Backpack, True);
        if FindQuantity < 5 then
        begin
            h := 5 - FindQuantity;
            FindTypeEx($097B, $FFFF, bank, True);
            if FindCount > 0 then MoveItem(FindItem, h, Backpack, 0,0,0);
            if FindCount <= 0 then AddToSystemJournal('В банке нет еды.');   
        end; 
        GoToGermes;
    end;
end;

procedure RunSell;
var
VX, VY, t : integer;
begin
    if dead and not Connected then exit;
    if CheckPlace = 1 then MoveOut;
    if CheckPlace = 2 then
    begin
        NewMoveXY(2568,518,True,0,False);
        repeat
            FindTypeEx($0191, $FFFF, Ground, True);
            if FindItem <> VendorID then Ignore(FindItem);
            wait(100); 
        until FindItem = VendorID;
        t := 50;
        if TargetPresent then CancelTarget;
        repeat
            VX := GetX(FindItem);
            VY := GetY(FindItem);
            NewMoveXY(VX,VY,True,0,CheckWeight);
            if not TargetPresent and (t >= 50) then
            begin
                UOSay('Sell Bag');
                t := 0;
            end;
            wait(100);
            t := t + 1;
        until TargetPresent or dead;
        WaitTargetObject(SellPack);
        repeat
            wait(100);
        until CheckGold;
        FindTypeEx($0EED, $0000, Backpack, True);
        AddToSystemJournal('Сумма продажи: '+IntToStr(FindFullQuantity - 300));       
    end;
end;

procedure CheckPlay;
var g, t, r : Integer;
begin
    if dead and not Connected then exit;
    Hungry(0,backpack);
    g := StrToInt(GetGlobal('stealth', 'NeedSell'));
    if g > 0 then
    begin
        t := 0;
        SetGlobal('stealth', 'NeedSell', IntToStr(Self));
        repeat
            if not TradeCheck(0, 1) then ConfirmTrade(0);
            wait(5000);
            g := StrToInt(GetGlobal('stealth', 'NeedSell'));
            r := 0;
            repeat
                if r >= 300 then 
                begin
                    repeat
                        if Connected then Disconnect;
                        wait(5000);
                    until not Connected;
                    wait(5000);
                    repeat
                        if not Connected then Connect;
                        wait(5000);
                    until Connected;         
                end;
                CheckLag(60000);
                FindTypeEx(ThisSell, $FFFF, Backpack, False);
                if FindCount > 0 then MoveItems(Backpack, ThisSell, $FFFF, SellPack, 0,0,0,50);
                r := r + 1;
                wait(1000);
            until FindCount <= 0;
            t := t + 1;
        until (g = 0) or (t >= 300);
        if t >= 3000 then exit; 
        RunSell;
        if CheckGold then DropGold;
        GoToGermes;
    end; 
end;

/////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////// Основной Скрипт //////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////

procedure TestDebag;
begin
    if not GetARStatus then SetARStatus(True);
    FindDistance := 18;
    moveThroughNPC := 0; 
    repeat
        while not WaitS do
        begin
            wait(10000);
        end;        
        if CheckSellM and Connected then RunSell;
        if CheckGold and Connected then DropGold;
        GoToGermes; 
        CheckPlay;   
        wait(5000);   
    until dead;
    AddToSystemJournal('Чар мёртв... отключаемся и завершаем скрипт');
    if GetARStatus then SetARStatus(False);
    Disconnect;
end;

begin
TestDebag;
end.