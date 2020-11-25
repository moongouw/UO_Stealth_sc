Program BS_Minoc;
{$Include 'all.inc'}
/////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////    Настройки    //////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////

const
AxeBag = $505A339E;      // Пак с кирками
CraftBag = $50846F18;    // Сумка для ремесла

/////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////  Начало блоков  //////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////

var
ing : array of Cardinal;
SkillBS : array of Cardinal;
GropID : array [0..20] of Cardinal;
s, m, CountGrop : integer;

procedure GetGrop;
var i, g : Integer;
begin
    for i := 0 to 20 do
    begin
        g := StrToInt(GetGlobal('stealth', IntToStr(i)));
        if (g >= 0) and (g <> GropID[i]) then
        begin
            GropID[i] := g;
            CountGrop := i;
        end;
    end;
end;

procedure OpenPack(OpenItem : Cardinal);
begin
    if dead and not Connected then exit;
    if (LastContainer <> OpenItem) and (dead = false) then
    begin
      repeat
        if not Connected then exit;
        CheckLag(5000);
        Checksave;
        UseObject(OpenItem);
        wait(1000);
      until LastContainer = OpenItem;
    end;
end;

function CheckSkillAndIngos (skill : String) : Boolean;
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
    for s := 0 to Length(SkillBS) - 1 do
    begin
        if GetSkillValue(skill) >= SkillBS[s] then
        begin
            if skill = 'Tinkering' then
            begin
                m := Length(SkillBS) - 1;
                result := True;
                exit;
            end;
            for m := 0 to Length(ing) - 1 do
            begin
                FindTypeEx($1BF2, ing[m], Backpack, True);
                if (FindQuantity < 18) and (s <= m) then 
                begin
                    Ignore(FindItem);
                    FindTypeEx($1BF2, ing[m], Backpack, True);
                end;
                if (FindQuantity >= 18) and (s <= m) then
                begin
                    result := True;
                    exit;
                end;
            end;
        end;
    end; 
end;

procedure Reload;
begin
    if Connected then Disconnect;
    repeat    
        wait(10000);
    until Connected;
end;

function CreatePickAxe : Boolean;
var
t : integer;
tm : TDateTime;
begin
    FindTypeEx($0E85,$FFFF, AxeBag, True);
    AddToSystemJournal('Кирки: '+IntToStr(FindCount));
    if (FindCount < 100) and CheckSkillAndIngos('Tinkering') then
    begin
        if not Connected or not CheckLag(5000) then exit;
        if MenuPresent then CloseMenu;
        if targetpresent then CancelTarget;
        if MenuHookPresent then CancelMenu();
        UseType($1EBC, $FFFF);
        WaitForTarget(5000)
        t := 0;
        repeat
            wait(100);
            t := t + 1;
        until TargetPresent or (t >= 150);
        AutoMenu('What','Deadly Tools');
        AutoMenu('What','Pickaxe');
        FindTypeEx($1BF2, ing[m], Backpack, False);
        WaitTargetObject(FindItem);
        t := 0;
        repeat
            wait(100);
            t := t + 1;
        until not TargetPresent or (t >= 150);
        tm := Now;
        t := 0;
        repeat
            wait(100);
            t := t + 1;
        until (InJournalBetweenTimes('skilled enough|Success|You destroyed|You stop.|You are already holding the item', tm, Now) <> -1) or (t >= 150);
        if InJournalBetweenTimes('You are already holding the item', tm, Now) <> -1 then Reload;
        if MenuHookPresent then CancelMenu();
        t := 0;
        repeat
            FindTypeEx($0E85, $FFFF, Backpack, False);
            if MoveItem(FindItem, 1, AxeBag, 0,0,0) then Ignore(FindItem);
            wait(100);
            t := t + 1;
        until (FindCount <= 0) or (t >150);
        IgnoreReset;
        exit;
    end;
    result := True;
end;

procedure SellM;
var 
g,tcont : Cardinal;
t, i : Integer;
begin
    SetGlobal('stealth', 'NeedSell', '1');
    t := 0;
    repeat
        g := StrToInt(GetGlobal('stealth', 'NeedSell'));
        t := t + 1;
        wait(1000);
    until (g > 1) or (t >= 6);
    if t >= 6 then exit;
    if IsTrade then CancelTrade(0);
    i := 0;
    repeat
        repeat
            while not IsTrade do
            begin
                t := 0;
                FindTypeEx($23C8, $FFFF, Backpack, True);
                if MoveItem(FindItem, 1, g, 0,0,0) then
                begin
                    t := t + 1;
                    Ignore(FindItem);
                end;
                wait(5000); 
            end;
            tcont := GetTradeContainer(0, 1);
            repeat
                FindTypeEx($23C8, $FFFF, Backpack, True);
                if IsTrade and (FindCount > 0) then
                begin
                    MoveItem(FindItem, 1, tcont, 1,1,0);
                    Ignore(FindItem);
                    t := t + 1;  
                    wait(500);
                end;
            until (t >= 30) or (IsTrade = False);
            wait(300);
        until IsTrade;
        i := i + t;      
        repeat
            if not TradeCheck(0, 1) then ConfirmTrade(0);
            wait(100);
        until not IsTrade;
    until i >= 120;
    if IsTrade then CancelTrade(0);
    SetGlobal('stealth', 'NeedSell', '0');
    
end;

function BS : Boolean;
var
t : integer;
tm : TDateTime;
begin
    FindTypeEx($23C8,$FFFF, CraftBag, True);
    AddToSystemJournal('Косы: '+IntToStr(FindCount));
    if (FindCount < 120) and CheckSkillAndIngos('Blacksmithing') then
    begin
        if not Connected or not CheckLag(5000) then exit;
        if MenuPresent then CloseMenu;
        if targetpresent then CancelTarget;
        if MenuHookPresent then CancelMenu();
        UseType($13E3, $FFFF);
        WaitForTarget(5000)
        t := 0;
        repeat
            wait(100);
            t := t + 1;
        until TargetPresent or (t >= 150);
        AutoMenu('What','Weapons');
        AutoMenu('What','Swords');
        AutoMenu('What','Scythe');
        FindTypeEx($1BF2, ing[m], Backpack, False);
        WaitTargetObject(FindItem);
        t := 0;
        repeat
            wait(100);
            t := t + 1;
        until not TargetPresent or (t >= 150);
        tm := Now;
        t := 0;
        repeat
            wait(100);
            t := t + 1;
        until (InJournalBetweenTimes('Success|You destroyed|You stop.', tm, Now) <> -1) or (t >= 150);
        if MenuHookPresent then CancelMenu();
        t := 0;
        tm := Now;
        repeat
            FindTypeEx($23C8, $FFFF, Backpack, False);
            if MoveItem(FindItem, 1, CraftBag, 0,0,0) then Ignore(FindItem);
            wait(100);
            t := t + 1;
        until (FindCount <= 0) or (InJournalBetweenTimes('container or the container is full|You are already holding the item', tm, Now) <> -1) or (t >150);
        if InJournalBetweenTimes('You are already holding the item', tm, Now) <> -1 then Reload;
        IgnoreReset;
        result := False;
        exit;
    end;
    result := True;
end;

procedure Trade;
var
t, i, o, p : integer;
begin
    GetGrop;
    for i := 0 to CountGrop do
    begin
        o := StrToInt(GetGlobal('stealth', 'DropOre'+IntToStr(i)));
        if o = 1 then
        begin
            if IsTrade then CancelTrade(0);
            SetGlobal('stealth', 'DropOre'+IntToStr(i), '2');
            t := 0;
            repeat
            o := StrToInt(GetGlobal('stealth', 'DropOre'+IntToStr(i)));
            if not TradeCheck(0, 1) then ConfirmTrade(0);
            wait(100);
            t := t + 1;
            //тут должна быть стаковАлка!
            until (o <= 0) or (t >= 5000);
            if t >= 5000 then SetGlobal('stealth', 'DropOre'+IntToStr(i), '0');
            MoveItems(Backpack, $1BF2, $FFFF, Backpack, 0,0,0, 100);   
        end;
        p := StrToInt(GetGlobal('stealth', 'GetPickaxe'+IntToStr(i)));
        if p = 1 then 
        begin
            if IsTrade then CancelTrade(0);
            SetGlobal('stealth', 'GetPickaxe'+IntToStr(i), '2');
            repeat
                FindTypeEx($0E85, $FFFF, Backpack, True);
                if FindCount < 20 then CreatePickAxe;
            until FindCount >= 20;
            t := 0;
            repeat
                while not IsTrade do
                begin
                    FindTypeEx($0E85, $FFFF, Backpack, True);
                    if MoveItem(FindItem, 1, GropID[i], 0,0,0) then 
                    begin
                        t := t + 1;
                    end;
                    wait(5000); 
                end;
                FindTypeEx($0E85, $FFFF, Backpack, True);
                if MoveItem(FindItem, 1, GetTradeContainer(0, 1), 1,1,0) then 
                begin
                    t := t + 1;
                    Ignore(FindItem); 
                end;
                wait(300);      
            until t >= 20;
            t := 0; 
            repeat
                if not TradeCheck(0, 1) then ConfirmTrade(0);
                wait(100);
                t := t + 1;
            until not IsTrade or (t >= 5000);
            if t >= 5000 then SetGlobal('stealth', 'DropOre'+IntToStr(i), '0');
            if IsTrade then CancelTrade(0);
            IgnoreReset;   
        end;    
    end;
end;

/////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////// Основной Скрипт //////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////

procedure Testdebag;
begin
    SetGlobal('stealth', 'NeedSell', '0');
    OpenPack(Backpack);
    OpenPack(AxeBag);
    OpenPack(CraftBag);
    if MenuHookPresent then CancelMenu();
    if IsTrade then CancelTrade(0);
    repeat
    if Weight <= 53000 then Trade;
    if CreatePickAxe then
        //if BS then SellM;
    wait(1000);
    Hungry(0,backpack); 
    until dead;
end;

begin
TestDebag;
end.