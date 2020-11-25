// v. 2.0 by nepret(c) from ZuluHotel
//10.2020
Program BS_Minoc;
{$include All.inc}
/////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////    Настройки    //////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
const
AxeBag = $4014FF2D;         // Пак с кирками
CraftBag = $4014F910;       // Сумка для ремесла
FindQuantityCraft = 18;     // Кол-во ингов для крафта предмета
CountCraftItem = 150;       // Сколько крафтить предметов за 1 заход 
CountCraftPickAxe = 135;    // Сколько крафтить кирок
TypePickAxe = $0E85;        // Type кирки
GivePickAxeCount = 45;      // Сколько кирок давать минерам
XAnvil = 4377;              // X координата возле плавильни
YAnvil = 2775;              // y координата возле плавильни
IDSeller = $0002B850;       // ID вендора которому продаём

var
CraftItem, tw   : integer;
TurnID      : Cardinal;
TurnNeed    : Integer;

procedure MenuBS;
begin
//////////// что куём? ///////////
    AutoMenu('What','Weapons'); //1
    AutoMenu('What','Polearms');  //2
    AutoMenu('What','Bardiche');  //3  
                                //...? можно добавлять дополнительные строки
//////////////////////////////////
end;

procedure CreatePickAxe;
var
t : integer;
tm : TDateTime;
begin
    if not Connected or not CheckLag(5000) then exit;
    if MenuPresent then CloseMenu;
    if targetpresent then CancelTarget;
    if MenuHookPresent then CancelMenu();
    AddToSystemJournal('Processed CreatePickAxe...');
    repeat   
        if dead or not Connected then exit;   
        CheckLag(60000);
        CheckSave;
        UseType($1EBC, $FFFF);
        WaitForTarget(5000);
    until TargetPresent;
    AutoMenu('What','Deadly Tools');
    AutoMenu('What','Pickaxe');
    FindTypeEx($1BF2, $0000, Backpack, False);
    WaitTargetObject(FindItem);
    tm := Now;
    t := 0;
    CheckSave;
    repeat     
        if dead or not Connected then exit;
        wait(100);
        t := t + 1;
    until (InJournalBetweenTimes('skilled enough|Success|You destroyed|You stop.|You are already holding the item', tm, Now) <> -1) or (t >= 150);
    wait(500);    
end;

procedure BS (ing : cardinal);
var
t : integer;
tm : TDateTime;
begin
    if not Connected or not CheckLag(5000) then exit;
    if MenuPresent then CloseMenu;
    if targetpresent then CancelTarget;
    if MenuHookPresent then CancelMenu();
    if dead or not Connected then exit;
    AddToSystemJournal('Processed BS...');
    if (GetType(ObjAtLayer(RhandLayer)) <> $13E3) then
    begin
        FindTypeEx($13E3, $FFFF, Backpack, False);
        if FindCount >= 0 then
        begin           
            CheckLag(60000); 
            CheckSave;
            Equip(RhandLayer,FindItem);
            wait(500);
        end;
        if FindCount <= 0 then
        begin
            AddToSystemJournal('ERROR: Отсутствует Smith Hammer!');
            exit;  
        end;                                                                                                                                                                                                                                                                           
    end;
    MenuBS;
    repeat        
        if dead or not Connected then exit;
        CheckSave;
        UseType($13E3, $FFFF);
        WaitForTarget(5000);
    until TargetPresent;
    WaitTargetObject(ing);
    tm := Now;
    t := 0;
    repeat             
        if dead or not Connected then exit;
        CheckSave;
        wait(100);
        t := t + 1;
    until (InJournalBetweenTimes('Success|You destroyed|You stop.', tm, Now) <> -1) or (t >= 150);
    wait(500);    
end;

procedure Stuck(color : Word);
var
s1 : Cardinal;
List: TStringList;
i: Integer;
begin
    repeat    
        if dead or not Connected then exit;
        FindTypeEx($1BF2, color, Backpack, False);
        List := TStringList.Create; 
        if GetFindedList(List) then 
            for i := 0 to List.Count - 1 do
                if GetQuantity(StrToInt('$'+List.Strings[i])) >= 60000 then Ignore(StrToInt('$'+List.Strings[i]));
        List.Free;     
        FindTypeEx($1BF2, color, Backpack, False);     
        if FindCount <= 1 then exit;
        s1 := FindItem; 
        Ignore(finditem);
        FindTypeEx($1BF2, color, Backpack, False);
        if FindCount >= 1 then
        begin  
            MoveItem(s1, 60000 - FindQuantity, finditem, 0,0,0); 
            wait(900);
            IgnoreReset;
        end;  
    until FindCount <= 1;
    IgnoreReset;
end;

function ChoseIngos : cardinal;
var
i : integer;
ing : array of Cardinal;
SkillBS : array of Cardinal;
begin
    if not Connected or not CheckLag(60000) or dead then exit;
    if Length(SkillBS) < 1 then
    begin
        SetLength(SkillBS, 10);
        SetLength(ing, 10);
    end;
    ing[9] := $0000;    // iron
    ing[8] := $0602;    // copper
    ing[7] := $0425;    // bd
    ing[6] := $050C;    // pagan
    ing[5] := $03E9;    // silver
    ing[4] := $0483;    // spectral  
    ing[3] := $054E;    // lava
    ing[2] := $04E7;    // icerock 
    ing[1] := $07EC;    // mythril
    ing[0] := $0487;    // basilisk
    SkillBS [9] := 0;
    SkillBS [8] := 60;
    SkillBS [7] := 70;
    SkillBS [6] := 80;
    SkillBS [5] := 90;
    SkillBS [4] := 100;
    SkillBS [3] := 105;
    SkillBS [2] := 110;
    SkillBS [1] := 115;
    SkillBS [0] := 117;
    for i := 0 to High(ing) do
    begin 
        if dead or not Connected then exit;
        Stuck(ing[i]);
    end;      
    IgnoreReset;
    for i := 0 to High(SkillBS) do
    begin 
        if dead or not Connected then exit;
        if GetSkillValue('Blacksmithy') >= SkillBS[i] then
        begin
            repeat  
                if dead or not Connected then exit;
                FindTypeEx($1BF2, ing[i], Backpack, False);
                if FindQuantity < FindQuantityCraft then 
                    if finditem > 0 then Ignore(FindItem);
            until (FindQuantity >= FindQuantityCraft) or (FindCount <= 0);
            if FindCount > 0 then 
            begin
                result := FindItem;
                IgnoreReset;
                exit;  
            end;
        end;
    end;   
end;

procedure GetTypeCraftItem;
var
craft : cardinal;
begin
    IgnoreReset;
    craft := ChoseIngos;
    repeat  
        if dead or not Connected then exit;             
        FindTypeEx($FFFF, $FFFF, Backpack, True);
        if FindCount > 0 then Ignore(FindItem);
    until FindCount <= 0;
    BS(craft);
    CheckLag(60000);
    CHeckSave;
    repeat  
        if dead or not Connected then exit;             
        FindTypeEx($1BF2, $FFFF, Backpack, True);
        if FindCount > 0 then Ignore(FindItem);
    until FindCount <= 0;  
    FindTypeEx($FFFF, $FFFF, Backpack, True); 
    if FindCount > 0 then CraftItem := GetType(FindItem);
    MoveItems(Backpack, CraftItem, $FFFF, CraftBag, 1,1,1, 300);
    IgnoreReset;
end;

procedure Sepor;
var
SL : TStringList;
begin
    if dead or not Connected then exit;
    SL := TStringList.Create;
    StrBreakApart(GetGlobal('stealth','nameID'), ',', SL);
    TurnID := StrToInt(SL.Strings[0]);
    TurnNeed := StrToInt(SL.Strings[1]);
    SL.Free;
end;

function CheckTurn : Boolean;
begin
    result := False; 
    if dead or not Connected then exit;
    if StrToInt(GetGlobal('stealth','nameID')) = 0 then exit;
    Sepor;
    result := True;
end;  

function TradeMiners : Boolean;
var
i, t : Integer;
begin
result := false;
    repeat  
        if dead or not Connected then exit;
        Sepor;
        if TurnNeed = 1 then
        begin
            CheckSave;
            CheckLag(60000); 
            if GetTradeOpponent(0) <> TurnID then CancelTrade(0);
            if not TradeCheck(0, 1) then ConfirmTrade(0);
            wait(1000);
        end;    
    until TurnNeed <> 1;
    if dead or not Connected then exit; 
    CheckSave;
    CheckLag(60000);
    while TradeCount > 0 do
        CancelTrade(0);   
 
    repeat     
        if dead or not Connected then exit;
        Sepor;
        if TurnNeed = 2 then
        begin
            CheckSave;
            CheckLag(60000);
            if GetTradeOpponent(i) <> TurnID then
            begin 
                if dead or not Connected then exit;
                FindTypeEx(TypePickAxe, $FFFF, AxeBag, False);
                if FindCount < 0 then Exit;
                MoveItem(FindItem, 1, TurnID, 0,0,0);
                CheckSave;
                CheckLag(60000);
                wait(5000);   
                for i := 0 to TradeCount - 1 do
                begin
                    if GetTradeOpponent(i) <> TurnID then CancelTrade(i);
                end;
            end; 
            t := 1;
            repeat  
                if dead or not Connected then exit;
                FindTypeEx(TypePickAxe, $FFFF, AxeBag, False);
                if FindCount <= 0 then exit;
                MoveItem(FindItem, 1, GetTradeContainer(0, 1), 1,1,1);
                CheckSave;
                CheckLag(60000);
                wait(1000);
                t := t + 1;
            until t >= GivePickAxeCount;
            wait(1000);
        end;    
    until t >= GivePickAxeCount;
    if not TradeCheck(0, 1) then ConfirmTrade(0);
    wait(1000);
    SetGlobal('stealth', 'nameID', IntToStr(0));
    FindTypeEx($1BF2, $FFFF, Backpack, False);
    if FindCount > 0 then MoveItems(Backpack, $1BF2, $FFFF, Backpack, 1,1,1, 300);
    result := true;
end;

function Sell : Boolean;
var
t : integer;
tm : TDateTime;
begin
    result := False;
    while (GetX(Self) <> GetX(IDSeller)) or (GetY(Self) <> GetY(IDSeller)) do
    begin 
        if dead or not Connected then exit;
        CheckSave;
        CheckLag(60000);
        newMoveXY(GetX(IDSeller), GetY(IDSeller), True, 0, False);   
    end;
    t := 0;
    UOSay('Sell Bag');
    repeat   
        if dead or not Connected then exit;
        CheckSave;
        CheckLag(60000);
        t := t + 1;
        wait(100);
    until TargetPresent or (t >= 150);
    if not TargetPresent or (t >= 150) then exit; 
    t := 0;      
    tm := Now;
    WaitTargetObject(CraftBag);
    repeat  
        if dead or not Connected then exit;
        CheckSave;
        wait(100);
        t := t + 1;
    until (InJournalBetweenTimes('Your sale total is', tm, Now) <> -1) or (t >= 350);
    while (GetX(Self) <> XAnvil) or (GetY(Self) <> YAnvil) do
    begin 
        if dead or not Connected then exit;
        newMoveXY(XAnvil, YAnvil, True, 0, False);
    end;
    result := True;   
end;

procedure test;
begin
    SetARStatus(true);
    FindDistance := 10;
    MoveCheckStamina := 0;
    MoveThroughNPC := 0;
    CancelMenu;
    SetGlobal('stealth', 'nameID', IntToStr(0));
    if not dead and Connected then
    begin
        OpenPack(Backpack);
        OpenPack(AxeBag);
        OpenPack(CraftBag);   
    end;
    if MenuHookPresent then CancelMenu();
    if IsTrade then CancelTrade(0);
    repeat
        while dead or not Connected do
        begin
            tw := 0; 
            AddToSystemJournal('Нет соединения с сервером...');
            repeat
                wait(1000);
                tw := tw + 1; 
            until Connected or (tw >= 300);
            if Connected then AddToSystemJournal('Соединение с сервером установлено!');
            wait(5000);
            if dead and Connected then
            begin    
                AddToSystemJournal('Чар мёртв...');
                exit;
            end;
            OpenPack(Backpack);
            OpenPack(AxeBag);
            OpenPack(CraftBag);
        end; 
        hungry(0,0); 
        //////////////////////
        ///////////тинкер
        FindTypeEx($0E85, $FFFF, Backpack, false);
        if FindCount > 0 then MoveItems(Backpack, $0E85, $FFFF, AxeBag, 1,1,1, 300);
        repeat 
            FindTypeEx($0E85, $FFFF, AxeBag, false);
            if FindCount < CountCraftPickAxe then 
            begin
                if not dead and Connected then
                begin
                    CreatePickAxe;
                    MoveItems(Backpack, $0E85, $FFFF, AxeBag, 1,1,1, 300);
                    FindTypeEx($0E85, $FFFF, AxeBag, false); 
                    AddToSystemJournal('CraftPickAxe: '+IntToStr(FindCount)+'/'+IntToStr(CountCraftPickAxe));
                end;
            end;
            if not dead or Connected then break;
        until FindCount >= CountCraftPickAxe;
        //////////////////////
        ///////////трейдер
        if not dead and Connected and CheckTurn then
        begin
            repeat         
                if dead or not Connected then break;
                wait(100);
            until TradeMiners;
        end;
        //////////////////////
        ///////////BS
        if CraftItem <= 0 then GetTypeCraftItem;
        FindTypeEx(CraftItem, $FFFF, Backpack, false);
        if FindCount > 0 then MoveItems(Backpack, CraftItem, $FFFF, CraftBag, 1,1,1, 300);
        FindTypeEx(CraftItem, $FFFF, CraftBag, false);
        AddToSystemJournal('CraftItem: '+IntToStr(FindCount)+'/'+IntToStr(CountCraftItem)); 
        if FindCount < CountCraftItem then
        begin           
            BS(ChoseIngos);      
        end
        else
        begin
            repeat
                wait(100);
                if dead or not Connected then break;
            until Sell;
        end;
    until False;  
end; 

begin
    test;
end.