"""
Demonstrates how to use the background scheduler to schedule a job that executes on 8:00 pm from mon to fri
"""

from datetime import datetime, timedelta
import time
import os

import urllib.request
import json

from apscheduler.schedulers.background import BackgroundScheduler


def tick():
    print('Tick! The time is: %s' % datetime.now())


def daka():
    body={'tsDate':1472618852203,'tsDateDes':'6e42a7b22dac333231d0f5034b32a7cab8e692'}
    jsonData = json.dumps(body).encode(encoding="utf-8")
    
    print(jsonData)
    
    req = urllib.request.Request(url='http://kaoqin.jd.com/kaoqin/checkIn',data=jsonData)
    
    req.add_header('Content-Type', 'application/json')
    req.add_header('X-Requested-With','XMLHttpRequest')
    
    cookie='TrackID=1Vj6WwzfSf12KAJ_Z3cK6IidQ0BlVZgzVg0iT7xfHyieBqlgsHT6sB9oDYoawfu54kjptD3mbA96EM12rBSTviMkF6fKR3wPydSZdPwG67xY; pinId=CWnUgf5JfOuSlqpkKWh6Fw; pin=engineer_s_m; unick=engi_s; _tp=3M1Ay0A5XK1pa2KtTkBr2Q%3D%3D; _pst=engineer_s_m; cn=61; unpl=V2_ZzNtbUoCF0J8DBJVLB1eBmIHEg1KUUASJlxDBilNWQJlBBRVclRCFXIUR11nG10UZwcZXUtcRxdFCHZWfhBfAW4CEVVyZ0Ildgh2XEsaXAZjChBUQlJBJUUOQmRLRQRaPwobABdnQRF0C0FdfhpUNWYzEFpHUkQXcg5AZDB3XUhkAxFZS1VKFXAKdlVLGg%3d%3d; __jdv=122270672|xadpc.com|t_348258029_|tuiguang|9def85e0f532451a9726be4cce463679; ipLoc-djd=1-2809-51226-0; ipLocation=%u5317%u4EAC; areaId=1; thor=A51C335A7F2900CB091F58B90F8588C122720D86EA4D46A7812D2BE29675A95F884E763E8FD8A23984AE05912AD87C0D9E81A42092F52BEF048A82A4B00D8D3FFC8A1C158F4510EF3E26A37A7B3A3D74DD32C1828B1D0AB91E4A15173799795871C8AA61E85BF55A863691AC18BD1E0B8A76D5ACC0CBF26703F69CDABF5E234C42CFC5DA86EEE930E07E94AAAEEA5BE9; erp1.jd.com=71CABA7D656A1A22C2A800D29C0619604681CF5210B3FEC7DEA568034DF377E9A6E9651EED99801DE0EC97B626DD02700071ABAB7B6A712E9B0957ADF5DBBC470EC9484C2D82EED4628A30FEA80BCFFC43F718CAA0F6E6701C708430E7921BE4; sso.jd.com=d658a907bfc6441dbf7d81aa032e0bf8; __jda=137720036.2125838143.1472541532.1472606677.1472618852.4; __jdb=137720036.1.2125838143|4.1472618852; __jdc=137720036; __jdu=2125838143; 3AB9D23F7A4B3C9B=43A0E5D431111391BF83AD54386BB4500ACA27DED0A1C3AE683BFC6049C6EAD20C3591ADE86A52E69E18B79ED7AD10FD'
    
    req.add_header('Cookie',cookie)
    
    with urllib.request.urlopen(req) as f:
        res=f.read().decode('utf-8')
        print(res)


if __name__ == '__main__':
    scheduler = BackgroundScheduler()

    scheduler.add_job(daka, 'cron', day_of_week='mon-fri',hour=18, minute=30)

    #alarm_time = datetime.now() + timedelta(seconds=10)
    #scheduler.add_job(tick, 'date', run_date=alarm_time)

    #scheduler.add_job(tick, 'interval', seconds=3)
    scheduler.start()
    print('Press Ctrl+{0} to exit'.format('Break' if os.name == 'nt' else 'C'))

    try:
        # This is here to simulate application activity (which keeps the main thread alive).
        while True:
            time.sleep(2)
    except (KeyboardInterrupt, SystemExit):
        # Not strictly necessary if daemonic mode is enabled but should be done if possible
        scheduler.shutdown()
