// v. 2.0 by nepret(c) from ZuluHotel
//10.2020
Program BS_Minoc;
{$include All.inc}
/////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////    Настройки    //////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
const
AxeBag = $5A30D15E;         // Пак с кирками
CraftBag = $5A30D2D8;       // Сумка для ремесла
FindQuantityCraft = 18;     // Кол-во ингов для крафта предмета
CountCraftItem = 120;       // Сколько крафтить предметов за 1 заход 
CountCraftPickAxe = 100;    // Сколько крафтить кирок

var
CraftItem : integer;

procedure MenuBS;
begin
//////////// что куём? ///////////
    AutoMenu('What','Weapons'); //1
    AutoMenu('What','Swords');  //2
    AutoMenu('What','Kryss');  //3  
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
        CheckSave;
        UseType($13E3, $FFFF);
        WaitForTarget(5000);
    until TargetPresent;
    WaitTargetObject(ing);
    tm := Now;
    t := 0;
    CheckSave;
    repeat
        wait(100);
        t := t + 1;
    until (InJournalBetweenTimes('Success|You destroyed|You stop.', tm, Now) <> -1) or (t >= 150);
    wait(500);    
end;

function ChoseIngos : cardinal;
var
i : integer;
ing : array of Cardinal;
SkillBS : array of Cardinal;
begin
    if Length(SkillBS) < 1 then
    begin
        SetLength(SkillBS, 10);
        SetLength(ing, 10);
    end;
    ing[9] := $0000;    // iron
    ing[8] := $08EB;    // copper
    ing[7] := $0425;    // bd
    ing[6] := $050C;    // pagan
    ing[5] := $04EB;    // silver
    ing[4] := $0483;    // spectral  
    ing[3] := $054E;    // lava
    ing[2] := $04E7;    // icerock 
    ing[1] := $07EC;    // mythril
    ing[0] := $0487;    // basilisk
    SkillBS [9] := 0;
    SkillBS [8] := 40;
    SkillBS [7] := 60;
    SkillBS [6] := 75;
    SkillBS [5] := 90;
    SkillBS [4] := 100;
    SkillBS [3] := 105;
    SkillBS [2] := 110;
    SkillBS [1] := 115;
    SkillBS [0] := 117;   
    for i := 0 to Length(SkillBS) - 1 do
    begin 
        if GetSkillValue('Blacksmithy') >= SkillBS[i] then
        begin
            repeat
                FindTypeEx($1BF2, ing[i], Backpack, False);
                if FindQuantity < FindQuantityCraft then Ignore(FindItem);
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
    craft := ChoseIngos;
    repeat               
        FindTypeEx($FFFF, $FFFF, Backpack, True);
        if FindCount > 0 then Ignore(FindItem);
    until FindCount <= 0;
    BS(craft);
    CheckLag(60000);
    CHeckSave;
    repeat               
        FindTypeEx($1BF2, $FFFF, Backpack, True);
        if FindCount > 0 then Ignore(FindItem);
    until FindCount <= 0;  
    FindTypeEx($FFFF, $FFFF, Backpack, True); 
    if FindCount > 0 then CraftItem := GetType(FindItem);
    MoveItems(Backpack, CraftItem, $FFFF, CraftBag, 1,1,1, 300);
    IgnoreReset;
end;

procedure CheckTurn;
begin
    if (StrToInt(GetGlobal('stealth','nameID')) <> 0) then
end;  

procedure test;
begin
    FindDistance := 10;
    SetGlobal('stealth', 'nameID', IntToStr(0));
    OpenPack(Backpack);
    OpenPack(AxeBag);
    OpenPack(CraftBag);
    if MenuHookPresent then CancelMenu();
    if IsTrade then CancelTrade(0);
    repeat
        hungry(0,0); 
        //////////////////////
        ///////////трейдер
        if StrToInt(GetGlobal('stealth','nameID')) <> 0 then 
        begin
            
        end;
        //////////////////////
        ///////////тинкер
        FindTypeEx($0E85, $FFFF, Backpack, false);
        if FindCount > 0 then MoveItems(Backpack, $0E85, $FFFF, AxeBag, 1,1,1, 300);
        repeat 
            FindTypeEx($0E85, $FFFF, AxeBag, false);
            if FindCount < CountCraftPickAxe then 
            begin
                CreatePickAxe;
                MoveItems(Backpack, $0E85, $FFFF, AxeBag, 1,1,1, 300);
                FindTypeEx($0E85, $FFFF, AxeBag, false); 
                AddToSystemJournal('CraftPickAxe: '+IntToStr(FindCount)+'/'+IntToStr(CountCraftPickAxe));
            end; 
        until FindCount >= CountCraftPickAxe;
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
        end;
    until dead;  
end; 

begin
    test;
end.