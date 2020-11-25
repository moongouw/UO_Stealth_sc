Program StuckAll;

procedure Stuck(types, color : Word);
var
s1 : Cardinal;
List: TStringList;
i: Integer;
begin
    repeat    
        if not Connected or dead then exit;
        if TargetPresent then CancelTarget;
        FindTypeEx(types, color, Backpack, False);
        List := TStringList.Create; 
        if GetFindedList(List) then 
            for i := 0 to List.Count - 1 do
                if GetQuantity(StrToInt('$'+List.Strings[i])) >= 60000 then Ignore(StrToInt('$'+List.Strings[i]));
        List.Free;     
        FindTypeEx(types, color, Backpack, False);     
        if FindCount <= 1 then exit;
        s1 := FindItem; 
        Ignore(finditem);
        FindTypeEx(types, color, Backpack, False);
        if FindCount >= 1 then
        begin   
            if TargetPresent then CancelTarget;
            MoveItem(s1, 60000 - FindQuantity, finditem, 0,0,0); 
            wait(900);
            IgnoreReset;
        end;  
    until FindCount <= 1;
    IgnoreReset;
end;

begin
    Stuck($FFFF, $FFFF);
end.