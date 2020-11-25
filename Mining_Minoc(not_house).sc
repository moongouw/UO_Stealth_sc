Program Mining_Cove;
{$include all.inc}
const
////////////////////////////////////////////////////////////////////////////
// 4 угла шахты, X и Y , minX\Y - минимальное значение, maxX\Y - максимальное 
minX = 2557;        //Минимальная координата по оси X
maxX = 2594;        //Максимальная координата по оси X
minY = 445;         //Минимальная координать по оси Y
maxY = 499;         //Максимальная координата по оси Y
////////////////////////////////////////////////////////////////////////////
BSX = 2593;         //Координаты X БСера с плавильней
BSY = 461;          //Координаты Y БСера с плавильней
BSer = $00012234;   //ID БСера
////////////////////////////////////////////////////////////////////////////
TypePickAxe = $0E85; //Type кирки


var
x9, y9              : Integer;
t9                  : Word;


function CheckTile (x, y : Word) : Word;
var
a : TStaticCell;
TileType : array [0..24] of Integer;
i, i0 : Integer;
begin
    TileType[0] := 1339; //типа тайлов шахты
    TileType[1] := 1340;
    TileType[2] := 1341;
    TileType[3] := 1342;
    TileType[4] := 1343;
    TileType[5] := 1344;
    TileType[6] := 1345;
    TileType[7] := 1346;
    TileType[8] := 1347;
    TileType[9] := 1348;
    TileType[10] := 1349;
    TileType[11] := 1350;
    TileType[12] := 1351;
    TileType[13] := 1352;
    TileType[14] := 1353;
    TileType[15] := 1354;
    TileType[16] := 1355;
    TileType[17] := 1356;
    TileType[18] := 1357;
    TileType[19] := 1358;
    TileType[20] := 1359;
    TileType[21] := 1386;
    TileType[22] := 1361;
    TileType[23] := 1362;
    TileType[24] := 1363;
    a := ReadStaticsXY(x, y, WorldNum);
    if a.StaticCount > 0 then
    begin
        for i := 0 to a.StaticCount - 1 do
        begin
            for i0 := 0 to 24 do
            begin
                if a.Statics[i].Tile = TileType[i0] then
                begin
                    result := TileType[i0];
                    exit;
                end;            
            end;   
        end;
    end;
    result := 0;
end;

function Mining (tile, x, y, z : Integer) : Boolean;
var
ctime : TDateTime;
txtR, txtF : String;
t : Integer;
begin
    result := False;
    txtF := 'There is no metal here to mine.';
    txtR := 'You stop mining.';
    repeat        
        if (GetType(ObjAtLayer(RhandLayer)) <> $0E86) then
        begin
            FindTypeEx(TypePickAxe, $FFFF, Backpack, False);
            if FindCount >= 0 then
            begin           
                CheckLag(60000); 
                CheckSave;
                Equip(RhandLayer,FindItem);
                wait(500);
            end;
            if FindCount <= 0 then exit;                                                                                                                                                                                                                                                                             
        end;
        t := 0;
        if TargetPresent then CancelTarget;
        ctime := Now;
        CheckLag(60000);
        UseObject(ObjAtLayerEx(RhandLayer,self));
        WaitForTarget(5000);
        if TargetPresent then TargetToTile(tile, x, y, z);
        repeat  
            CheckSave;
            wait(100);
        until (InJournalBetweenTimes(txtR, ctime, Now) <> -1);
    until (InJournalBetweenTimes(txtF, ctime, Now) <> -1);
    result := True;
end;

procedure ProcessedResource;
var
ctime : TDateTime;
begin
    newMoveXY(BSX, BSY, True, 0, True);
    repeat        
        FindTypeEx($19B9, $FFFF, Backpack,False);  
        if FindCount > 0 then
        begin  
            repeat 
                ctime := Now;
                CheckSave;
                CheckLag(60000);
                UseObject(finditem);   
                wait(300);
            until (InJournalBetweenTimes('Success:|Failed.', ctime, Now) <> -1);
        end;
    until FindCount <= 0; 
end;

procedure ProcessedTrade;
var
t : integer;
begin
    if IsTrade then CancelTrade(0);
    repeat
        FindTypeEx($1BF2, $FFFF, Backpack, True);
        if FindCount > 0 then MoveItem(FindItem, 2000, BSer, 0,0,0);
        wait(1000);
        t := 0;
        repeat
            CheckSave;
            if not TradeCheck(0, 1) then ConfirmTrade(0);
            wait(100);
            t := t + 1;
        until not IsTrade or (t >= 100);          
    until FindCount <= 0;
    SetGlobal('stealth', 'nameID', IntToStr(1));
    wait(1000);
    while(StrToInt(GetGlobal('stealth','nameID')) = 1) do
    begin
        if not TradeCheck(0, 1) then ConfirmTrade(0);
        wait(100);
    end;  
end;

procedure DropMaps;
begin
    FindTypeEx($14ED, $FFFF, backpack, False);
    if FindCount <= 0 then exit;
    FindTypeEx($0E77, $FFFF, Ground, False);
    if FindCount > 0 then 
    begin
        newMoveXY(GetX(finditem), GetY(finditem), True, 1, False);
        MoveItems(Backpack, $14ED, $FFFF, finditem, 1,1,1, 300);
    end;
end;

procedure TurnNumber; //занимаем место в очереди
begin
    repeat
        while(StrToInt(GetGlobal('stealth','nameID')) <> 0) do
        begin
            wait(1000);
        end;      
        SetGlobal('stealth', 'nameID', IntToStr(Self));
        wait(1000);
    until StrToInt(GetGlobal('stealth','nameID')) = Self;
end;

begin
    FindDistance := 10; 
    MoveCheckStamina := 0;
    MoveThroughNPC := 0;
    if (minX > maxX) and (minY > maxY) then
    begin   
        AddToSystemJournal('ERROR: проверьте настройки координат шахты! "min" не может быть выше "max"!');
        exit;   
    end;
    while not dead do
    begin 
        for y9 := minY to maxY do
        begin
            for x9 := minX to maxX do
            begin
                t9 := CheckTile(x9, y9);
                if t9 > 0 then
                begin
                    if newMoveXY(x9, y9, True, 2, False) then
                    begin 
                        hungry(0,0);
                        if not Mining(t9, x9, y9, GetZ(Self)) then
                        begin 
                            ProcessedResource; 
                            TurnNumber;      
                            ProcessedTrade;
                            DropMaps;
                        end;     
                    end;
                end;          
            end;
        end;  
    end; 
end.
