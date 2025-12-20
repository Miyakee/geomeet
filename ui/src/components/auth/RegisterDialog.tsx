import * as React from 'react';
import { useState } from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogActions from '@mui/material/DialogActions';
import {DialogContent, TextField} from "@mui/material";
import {authApi, LoginRequest, RegisterRequest} from "../../services/api.ts";

interface RegisterDialogProps {
    open: boolean;
    onClose: () => void;
}

export const RegisterDialog = ({ open, onClose }: RegisterDialogProps) => {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [verificationCode, setVerificationCode] = useState('2025'); // 固定验证码

    const handleSignUp = async () => {
        if (!username || !email || !verificationCode || !password ) {
            alert('input required fields');
            return;
        }

        if (verificationCode !== '2025') {
            alert('verification code error，please input：2025');
            return;
        }
        const registerInfo: RegisterRequest = {
            username,
            password,
            email,
            verificationCode
        };

        await authApi.register(registerInfo);

        setUsername('');
        setPassword('');
        setEmail('');
        setVerificationCode('1234');
        onClose();
    };

    const handleClose = () => {
        setUsername('');
        setEmail('');
        setVerificationCode('2025');
        onClose();
    };

    return (
            <Dialog
                onClose={handleClose}
                open={open}
                maxWidth="xs"
                fullWidth
            >
                <DialogTitle sx={{ m: 0, p: 2 }} id="customized-dialog-title">
                   Sign Up
                </DialogTitle>
                <DialogContent>
                    <TextField 
                        fullWidth 
                        id="username"
                        label="Username"
                        variant="outlined"  
                        margin="normal"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                    />
                    <TextField
                        fullWidth
                        id="password"
                        label="Password"
                        variant="outlined"
                        margin="normal"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                    <TextField 
                        fullWidth 
                        id="email"
                        label="Email"
                        variant="outlined"  
                        margin="normal"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                    <TextField 
                        fullWidth 
                        id="code"
                        label="Code" 
                        variant="outlined" 
                        margin="normal"
                        value={verificationCode}
                        onChange={(e) => setVerificationCode(e.target.value)}
                        helperText="verification code ：2025"
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose}>Cancel</Button>
                    <Button onClick={handleSignUp} variant="contained">Register</Button>
                </DialogActions>
            </Dialog>
    );
}
