from django.contrib import auth
from django.contrib.auth import get_user
from django.contrib.auth.backends import ModelBackend
from django.contrib.auth.decorators import login_required
from django.db import IntegrityError
from django.http import HttpResponse
from django.http import JsonResponse
from django.shortcuts import render, redirect
from account_app import models
import time
import json

# Create your views here.
def login(request):
    if request.method == "POST":
        email = request.POST.get('email')
        password = request.POST.get('password')
        try:
            re = models.MyUser.objects.get(email=email)
        except models.MyUser.DoesNotExist:
            res = {'msg': 'fail','info':'email not found.'}
            return HttpResponse(json.dumps(res))
        re = auth.authenticate(request, username=email, password=password)
        if re is None:
            res = {'msg': 'fail', 'info': 'password is invalid.'}
            return HttpResponse(json.dumps(res))
        auth.login(request,re)
        res = {'msg':'success'}
        return HttpResponse(json.dumps(res))


def register(request):
    if request.method == "POST":
        email = request.POST.get('email')
        password = request.POST.get('password')
        username = request.POST.get('username')
        try:
            re = models.MyUser.objects.get(email=email)
            res = {'msg': 'fail','info':'email already taken.'}
            return HttpResponse(json.dumps(res))
        except models.MyUser.DoesNotExist:
            try:
                re = models.MyUser.objects.get(username=username)
                res = {'msg': 'fail', 'info': 'username already taken.'}
                return HttpResponse(json.dumps(res))
            except models.MyUser.DoesNotExist:
                user = models.MyUser()
                user.email = email
                user.set_password(password)
                user.username = username
                user.created_at = time.strftime('%Y-%m-%d', time.localtime(time.time()));
                user.save()
                res = {'msg': 'success'}
                return HttpResponse(json.dumps(res))

@login_required
def logout(request):
    # if request.method == "POST":
    auth.logout(request)
    # return render(request,'login.html')
    return redirect('login')

def getUsername(request):
    if request.method == "GET":
        email = request.GET.get('email')
        re = models.MyUser.objects.get(email=email)
        res = {'username': re.username}
        return HttpResponse(json.dumps(res))

def getbook(request):
    email = request.GET.get("email")
    if(email=="none"):
        return JsonResponse({'num':-1})
    else:
        user = models.MyUser.objects.get(email=email)
        results = models.book.objects.filter(user=user)
        json_ = {}
        json_['num'] = results.count()
        index=0
        for result in results:
            json_[index] = result.word
            index += 1
        return JsonResponse(json_)

def addword(request):
    email = request.POST.get("email")
    word = request.POST.get("word")
    user = models.MyUser.objects.get(email=email)
    book = models.book(user=user,word=word)
    book.save()
    return HttpResponse('')

def removeword(request):
    email = request.POST.get("email")
    word = request.POST.get("word")
    user = models.MyUser.objects.get(email=email)
    book = models.book.objects.get(user=user,word=word)
    book.delete()
    return HttpResponse('')