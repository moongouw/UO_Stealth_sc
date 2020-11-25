Program CheckEvil;


procedure CheckEvil;
var i : Integer;
begin
    FindTypeEx($0190, $FFFF, Ground, True);
    if GetName(FindItem) = '' then exit;
    if FindCount > 0 then
    begin
        i := GetNotoriety(FindItem);
        AddToSystemJournal('___________________________________');
        AddToSystemJournal('Нашли тело, ник тела: "'+GetName(FindItem)+'", проверяем на злобность');
        if (i <> 1) and (i <> 4) and (i <> 6) then AddToSystemJournal (IntToStr(GetNotoriety(FindItem)));
        if i = 1 then AddToSystemJournal(GetName(FindItem)+': синий ('+IntToStr(GetNotoriety(FindItem))+')');
        if i = 4 then AddToSystemJournal(GetName(FindItem)+': серый ('+IntToStr(GetNotoriety(FindItem))+')');
        if i = 6 then AddToSystemJournal(GetName(FindItem)+': ПК ('+IntToStr(GetNotoriety(FindItem))+')');
        Ignore(FindItem);
    end;
end;

begin
    FindDistance := 18;
    IgnoreReset;
    repeat
        CheckEvil;
        wait(100);
    until dead;   
end.