package Semaphor;

sub create {
    local($IPC_KEY) = @_;
    local($semid) ;
    $IPC_CREATE = 0001000;
    $semid = semget( $IPC_KEY, 1, 0666 | $IPC_CREATE ) ;
    die "semaphor-semget failed" if !defined($semid) ; 

    $semnum = 0;      $semflag = 0; 
    $opstring = pack("sss", $semnum, $semop = 1, $semflag) ; 
    semop($semid, $opstring) || die "$! " ;     #setting the values
    return $semid;         # this returns something
}

sub take {
    local($IPC_KEY) = @_ ;
    local($semid) ;
    $semid = semget( $IPC_KEY,  0 , 0 );
    die if !defined($semid);
    
    $semnum = 0; $semflag = 0;
    $opstring = pack("sss", $semnum, $semop = -1, $semflag) ; 
    semop($semid, $opstring) || die "semop failed in $! " ;    #setting the values
}

sub give {
    local($IPC_KEY) = @_;
    local($semid) ;
    $semid = semget( $IPC_KEY,  0 , 0 );
    die if !defined($semid);
    
    $semnum = 0; $semflag = 0;
    $opstring = pack("sss", $semnum, $semop = 1, $semflag) ; 
    semop($semid, $opstring) || die "semop failed in $! " ;   #setting the values

}    

1;
