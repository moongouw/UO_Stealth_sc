Program AutoLoot;
const
waitM = 200;        //Задержка лутания
Cont = 0;   //ID контейнера дляч лута или 0 если Backpack
//список того что не лутаем
LootIgnorList = '$1517,$0E28,$141B,$154B,$1452,$0E73,$144E,$1450,$1545,$1547,$0E9B,$1451,$423A,$0F8A,$2306,$1F0B,$0F0E,$0FC7,$144F';
var
LIL : array of Word;
CT : Cardinal;

procedure LootArrow (place : Cardinal);
var
arrow : array of Word;
i : Integer;
begin
    arrow := [$1BFB,$0F3F,$19B9];
    for i := 0 to High(arrow) do
    begin   
        repeat 
            if Dead then exit;
            if not Connected then exit;
            if (GetDistance(FindItem) > 2) then exit;
            FindTypeEx(arrow[i], $FFFF, place, False); 
            if (FindCount > 0) and (GetDistance(FindItem) <= 2) then
            begin  
                CheckLag(60000);
                if (GetDistance(FindItem) > 2) then exit;
                if MoveItem(FindItem, FindQuantity, Backpack, 0,0,0) then
                begin            
                    //Ignore(FindItem);
                    wait(waitM);  
                end;
            end;
        until (FindCount <= 0) or (GetDistance(FindItem) > 2);  
    end;
end;

function LootCourps (Courps : Cardinal) : Boolean;
var
i : Integer;
begin
    result := True;
    LootArrow(Courps);
    repeat
        FindTypeEx($FFFF, $FFFF, Courps, False);
        if FindCount > 0 then
        begin
            for i := 0 to High(LIL) do
            begin 
            if (GetDistance(FindItem) > 2) then exit;  
                if GetType(FindItem) = LIL[i] then 
                begin
                    Ignore(FindItem);
                    break;
                end;
            end;
            if i > High(LIL) then 
            begin
                if (GetDistance(FindItem) > 2) then exit;   
                CheckLag(60000);
                MoveItem(FindItem, FindQuantity, CT, 0,0,0);  
                Ignore(FindItem);
                wait(WaitM);
            end;          
            FindTypeEx($FFFF, $FFFF, Courps, False);
        end;
    until FindCount <= 0;
    if FindCount <= 0 then 
    begin
        Ignore(Courps); 
        result := False;
    end;
end;

procedure FindCourps;
begin
    if (LastContainer <> 0) and (GetType(LastContainer) = $2006) then
    begin
        LootCourps(LastContainer);
    end;
    wait(100);    
end;

procedure RunSettings;
var
SL : TStringList;
i : Integer;
begin
    SL := TStringList.Create;
    StrBreakApart(LootIgnorList, ',', SL);
    for i := 0 to SL.Count - 1 do
    begin
        SetLength(LIL, i + 1);
        LIL[i] := StrToInt(SL.Strings[i]);     
    end;       
    SL.Free;
    if Cont <= 0 then CT := Backpack; 
    if Cont > 0 then CT := Cont;
    FindDistance := 2; 
end;

procedure test;
var i : Integer;
begin
    for i := 0 to 10 do
end;

begin
    RunSettings;
    while True do
    begin
        if not dead or not Connected then 
        begin 
            FindCourps;
            LootArrow(Ground);
        end;
        wait(100);
    end;
end.